package tictim.paraglider;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.bargain.BargainRecipeChecker;
import tictim.paraglider.config.*;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderVillageStructures;
import tictim.paraglider.contents.recipe.WaterBottleIngredientType;
import tictim.paraglider.impl.DefaultParagliderItemCapability;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.impl.movement.PlayerStateMapLoader;
import tictim.paraglider.impl.stamina.StaminaEfficiencyLogicHandlerImpl;
import tictim.paraglider.impl.stamina.StaminaEfficiencyLogicLoader;
import tictim.paraglider.impl.stamina.StaminaLoader;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.network.ParagliderNetworkImpl;
import tictim.paraglider.plugin.ParagliderPluginLoader;
import tictim.paraglider.wind.WindSource;
import tictim.paraglider.wind.WindSourceRegistry;

@NullMarked
@Mod(ParagliderAPI.MODID)
public class ParagliderMod {
	public static final Logger LOGGER = LogManager.getLogger("Paraglider");

	private static @Nullable ParagliderMod instance;

	public static ParagliderMod instance() {
		if (instance == null) throw new IllegalStateException("Mod instance not ready yet");
		return instance;
	}

	{
		if (instance != null) throw new IllegalStateException("Paraglider mod instantiated twice");
		instance = this;
	}

	private final CommonConfig commonCfg = new CommonConfig();
	private final Contents contents;
	private final ServerConfig config = new ServerConfig();

	private final ParagliderNetworkImpl network;
	private final NeoForgeParagliderPluginLoader pluginLoader = new NeoForgeParagliderPluginLoader();

	private final PlayerStateMapConfig stateMapConfig;
	private final PlayerStateConnectionMap connectionMap;

	private final WindSourceRegistry windSourceRegistry = new WindSourceRegistry();

	private final @Nullable String staminaFactoryOrigin;

	@ApiStatus.Internal
	public @Nullable IClient client;

	public ParagliderMod(ModContainer modContainer, IEventBus eventBus) {
		this.contents = new Contents(eventBus);

		ParagliderAPI.setMovementSupplier(p -> p.getData(Contents.get().playerMovement()));
		ParagliderAPI.setStaminaSupplier(p -> p.getData(Contents.get().playerMovement()).stamina());
		ParagliderAPI.setVesselContainerSupplier(p -> p.getData(Contents.get().vesselContainer()));

		var pair = PlayerStateMapLoader.loadStates();
		this.stateMapConfig = new PlayerStateMapConfig(pair.getFirst());
		this.connectionMap = pair.getSecond();
		var pair2 = StaminaLoader.loadStaminaFactory();
		ParagliderAPI.setStaminaFactory(pair2.getFirst());
		this.staminaFactoryOrigin = pair2.getSecond();
		ParagliderAPI.setStaminaEfficiencyLogicHandler(new StaminaEfficiencyLogicHandlerImpl(
				StaminaEfficiencyLogicLoader.loadStaminaEfficiencyLogics()));

		ParagliderAPI.setDefaultParagliderItemCapability(new DefaultParagliderItemCapability());

		modContainer.registerConfig(ModConfig.Type.COMMON, this.commonCfg.spec);
		modContainer.registerConfig(ModConfig.Type.SERVER, this.config.spec);
		modContainer.registerConfig(ModConfig.Type.COMMON, this.stateMapConfig.spec, PlayerStateMapConfig.FILENAME);

		eventBus.addListener((NewRegistryEvent event) -> {
			event.create(new RegistryBuilder<>(BargainPreview.TYPE_REGISTRY_KEY).sync(true));
		});

		eventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
			event.dataPackRegistry(BargainTypeRegistry.REGISTRY_KEY, BargainType.CODEC);
			event.dataPackRegistry(WindSourceRegistry.REGISTRY_KEY, WindSource.CODEC, WindSource.CODEC);
		});

		eventBus.addListener((EntityAttributeModificationEvent event) -> {
			event.add(EntityTypes.PLAYER, this.contents.maxStamina());
			event.add(EntityTypes.PLAYER, this.contents.staminaEfficiency());
			event.add(EntityTypes.PLAYER, this.contents.staminaRecovery());
			event.add(EntityTypes.PLAYER, this.contents.paraglidingStaminaEfficiency());
			event.add(EntityTypes.PLAYER, this.contents.runningStaminaEfficiency());
			event.add(EntityTypes.PLAYER, this.contents.swimmingStaminaEfficiency());
		});

		NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> {
			this.stateMapConfig.removeCallbacks();
			this.stateMapConfig.reload(null);
			ParagliderUtils.printPlayerStates(this.stateMapConfig.stateMap(), getPlayerConnectionMap());
			this.stateMapConfig.addCallback(stateMap -> {
				ParagliderUtils.printPlayerStates(stateMap, getPlayerConnectionMap());
				ParagliderNetwork.get().syncStateMapToAll(stateMap);
			});
		});

		NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(ParagliderCommands.register()));
		NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> this.stateMapConfig.removeCallbacks());
		NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
			WindSourceRegistry.get().computeWindSource(event.getRegistries());
		});

		NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> {
			event.addListener(ParagliderAPI.id("wind_source_registry"),
					new WindSourceRegistry.ReloadListener(this.windSourceRegistry));
			event.addListener(ParagliderAPI.id("village_structure_injector"),
					new ParagliderVillageStructures.ReloadListener());
			event.addListener(ParagliderAPI.id("bargain_recipe_checker"),
					new BargainRecipeChecker(event.getServerResources().getRecipeManager()));
		});

		NeoForge.EVENT_BUS.addListener((RegisterBrewingRecipesEvent event) -> {
			event.getBuilder().addRecipe(new Ingredient(WaterBottleIngredientType.INSTANCE),
					Ingredient.of(Items.CARROT), new ItemStack(this.contents.energizingElixir2()));

			event.getBuilder().addRecipe(Ingredient.of(this.contents.energizingElixir2()),
					Ingredient.of(Items.GLOWSTONE_DUST), new ItemStack(this.contents.energizingElixir3()));

			event.getBuilder().addRecipe(Ingredient.of(this.contents.energizingElixir2()),
					Ingredient.of(Items.GOLDEN_CARROT), new ItemStack(this.contents.enduringElixir2()));

			event.getBuilder().addRecipe(Ingredient.of(this.contents.enduringElixir2()),
					Ingredient.of(Items.GLOWSTONE_DUST), new ItemStack(this.contents.enduringElixir3()));

			event.getBuilder().addRecipe(Ingredient.of(this.contents.energizingElixir3()),
					Ingredient.of(Items.GOLDEN_CARROT), new ItemStack(this.contents.enduringElixir3()));
		});

		this.network = new ParagliderNetworkImpl(eventBus);
	}

	public Cfg getConfig() {
		return this.config;
	}

	public DebugCfg getDebugConfig() {
		return this.commonCfg;
	}

	public FeatureCfg getFeatureConfig() {
		return this.commonCfg;
	}

	public Contents getContents() {
		return this.contents;
	}

	public ParagliderNetwork getNetwork() {
		return this.network;
	}

	public ParagliderPluginLoader getPluginLoader() {
		return this.pluginLoader;
	}

	public PlayerStateMap getPlayerStateMap() {
		if (this.client != null) {
			PlayerStateMap m = this.client.getSyncedStateMap();
			if (m != null) return m;
		}
		return getLocalPlayerStateMap();
	}

	public PlayerStateMap getLocalPlayerStateMap() {
		return this.stateMapConfig.stateMap();
	}

	public PlayerStateConnectionMap getPlayerConnectionMap() {
		return this.connectionMap;
	}

	public PlayerStateMapConfig getPlayerStateMapConfig() {
		return this.stateMapConfig;
	}

	public WindSourceRegistry windSourceRegistry() {
		return this.windSourceRegistry;
	}

	public @Nullable String staminaFactoryOrigin() {
		return this.staminaFactoryOrigin;
	}

	@ApiStatus.Internal
	public interface IClient {
		@Nullable PlayerStateMap getSyncedStateMap();
	}
}
