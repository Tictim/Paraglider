package tictim.paraglider;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import tictim.paraglider.impl.ParagliderCauldronInteraction;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.impl.movement.PlayerStateMapLoader;
import tictim.paraglider.impl.stamina.StaminaEfficiencyLogicHandlerImpl;
import tictim.paraglider.impl.stamina.StaminaEfficiencyLogicLoader;
import tictim.paraglider.impl.stamina.StaminaFactoryLoader;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.network.ParagliderNetworkImpl;
import tictim.paraglider.plugin.ParagliderPluginLoader;
import tictim.paraglider.wind.WindSource;
import tictim.paraglider.wind.WindSourceRegistry;

@Mod(ParagliderAPI.MODID)
public class ParagliderMod {
	public static final Logger LOGGER = LogManager.getLogger("Paraglider");

	private static ParagliderMod instance;

	public static @NotNull ParagliderMod instance() {
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
		ParagliderAPI.setStaminaFactory(StaminaFactoryLoader.loadStaminaFactory());
		ParagliderAPI.setStaminaEfficiencyLogicHandler(new StaminaEfficiencyLogicHandlerImpl(
				StaminaEfficiencyLogicLoader.loadStaminaEfficiencyLogics()));

		ParagliderAPI.setDefaultParagliderItemCapability(new DefaultParagliderItemCapability());

		modContainer.registerConfig(ModConfig.Type.COMMON, this.commonCfg.spec);
		modContainer.registerConfig(ModConfig.Type.SERVER, this.config.spec);
		modContainer.registerConfig(ModConfig.Type.COMMON, this.stateMapConfig.spec, PlayerStateMapConfig.FILENAME);

		eventBus.addListener((ModConfigEvent.Reloading event) -> {
			if (event.getConfig().getSpec() == this.stateMapConfig.spec) {
				this.stateMapConfig.scheduleReload(ServerLifecycleHooks.getCurrentServer(), null);
			}
		});

		eventBus.addListener((NewRegistryEvent event) -> {
			event.create(new RegistryBuilder<>(BargainPreview.TYPE_REGISTRY_KEY).sync(true));
		});

		eventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
			event.dataPackRegistry(BargainTypeRegistry.REGISTRY_KEY, BargainType.CODEC);
			event.dataPackRegistry(WindSourceRegistry.REGISTRY_KEY, WindSource.CODEC, WindSource.CODEC);
		});

		eventBus.addListener((EntityAttributeModificationEvent event) -> {
			event.add(EntityType.PLAYER, this.contents.maxStamina());
			event.add(EntityType.PLAYER, this.contents.staminaEfficiency());
			event.add(EntityType.PLAYER, this.contents.staminaRecovery());
			event.add(EntityType.PLAYER, this.contents.paraglidingStaminaEfficiency());
			event.add(EntityType.PLAYER, this.contents.runningStaminaEfficiency());
			event.add(EntityType.PLAYER, this.contents.swimmingStaminaEfficiency());
		});

		NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> {
			MinecraftServer server = event.getServer();
			PlayerStateMapConfig stateMapConfig1 = this.stateMapConfig;
			stateMapConfig1.removeCallbacks();
			stateMapConfig1.reload();
			ParagliderUtils.printPlayerStates(stateMapConfig1.stateMap(), getPlayerConnectionMap());
			stateMapConfig1.addCallback(stateMap -> {
				ParagliderUtils.printPlayerStates(stateMap, getPlayerConnectionMap());
				ParagliderNetwork.get().syncStateMapToAll(server, stateMap);
			});
		});

		NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(ParagliderCommands.register()));
		NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> this.stateMapConfig.removeCallbacks());

		NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
			event.addListener(new WindSourceRegistry.ReloadListener(this.windSourceRegistry, event.getRegistryAccess()));
			event.addListener(new ParagliderVillageStructures.ReloadListener(event.getRegistryAccess()));
			event.addListener(new BargainRecipeChecker(event.getRegistryAccess(), event.getServerResources().getRecipeManager()));
		});

		eventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
			var map = CauldronInteraction.WATER.map();
			map.put(this.contents.paraglider(), ParagliderCauldronInteraction.INSTANCE);
			map.put(this.contents.dekuLeaf(), ParagliderCauldronInteraction.INSTANCE);
		}));

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

	public @NotNull Cfg getConfig() {
		return this.config;
	}

	public @NotNull DebugCfg getDebugConfig() {
		return this.commonCfg;
	}

	public @NotNull FeatureCfg getFeatureConfig() {
		return this.commonCfg;
	}

	public @NotNull Contents getContents() {
		return this.contents;
	}

	public @NotNull ParagliderNetwork getNetwork() {
		return this.network;
	}

	public @NotNull ParagliderPluginLoader getPluginLoader() {
		return this.pluginLoader;
	}

	public @NotNull PlayerStateMap getPlayerStateMap() {
		if (this.client != null) {
			PlayerStateMap m = this.client.getSyncedStateMap();
			if (m != null) return m;
		}
		return getLocalPlayerStateMap();
	}

	public @NotNull PlayerStateMap getLocalPlayerStateMap() {
		return this.stateMapConfig.stateMap();
	}

	public @NotNull PlayerStateConnectionMap getPlayerConnectionMap() {
		return this.connectionMap;
	}

	public @NotNull PlayerStateMapConfig getPlayerStateMapConfig() {
		return this.stateMapConfig;
	}

	public @NotNull WindSourceRegistry windSourceRegistry() {
		return this.windSourceRegistry;
	}

	@ApiStatus.Internal
	public interface IClient {
		@Nullable PlayerStateMap getSyncedStateMap();
	}
}
