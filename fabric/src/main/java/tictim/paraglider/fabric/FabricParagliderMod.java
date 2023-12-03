package tictim.paraglider.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.command.ParagliderCommands;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.config.PlayerStateMapConfig;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderVillageStructures;
import tictim.paraglider.fabric.client.FabricParagliderClient;
import tictim.paraglider.fabric.config.FabricCommonConfig;
import tictim.paraglider.fabric.config.FabricConfig;
import tictim.paraglider.fabric.config.FabricPlayerStateMapConfig;
import tictim.paraglider.fabric.contents.FabricBargainTypeRegistry;
import tictim.paraglider.fabric.contents.FabricContents;
import tictim.paraglider.fabric.contents.loot.FabricLootTable;
import tictim.paraglider.fabric.contents.loot.ParagliderLoots;
import tictim.paraglider.fabric.event.ParagliderEventHandler;
import tictim.paraglider.fabric.impl.PlayerMovementAccess;
import tictim.paraglider.impl.movement.PlayerStateConnectionMap;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.impl.movement.PlayerStateMapLoader;
import tictim.paraglider.impl.stamina.StaminaFactoryLoader;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.plugin.ParagliderPluginLoader;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindUtils;

import java.util.Objects;

@SuppressWarnings("unused")
public final class FabricParagliderMod extends ParagliderMod implements ModInitializer{
	@NotNull public static FabricParagliderMod get(){
		return (FabricParagliderMod)ParagliderMod.instance();
	}

	private final FabricContents contents = FabricContents.create();
	private final FabricConfig config = new FabricConfig();
	private final FabricCommonConfig commonConfig = new FabricCommonConfig();

	@Nullable private FabricPlayerStateMapConfig stateMapConfig;
	@Nullable private PlayerStateConnectionMap connectionMap;

	@Override public void onInitialize(){
		this.contents.register();
		ParagliderLoots.register();

		CauldronInteraction.WATER.put(Contents.get().paraglider(), CauldronInteraction.DYED_ITEM);
		CauldronInteraction.WATER.put(Contents.get().dekuLeaf(), CauldronInteraction.DYED_ITEM);

		FabricParagliderNetwork.init();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ParagliderVillageStructures.addVillageStructures(server);
			ParagliderUtils.checkBargainRecipes(server);

			FabricPlayerStateMapConfig stateMapConfig = Objects.requireNonNull(this.stateMapConfig);
			stateMapConfig.removeCallbacks();
			stateMapConfig.reload();
			ParagliderUtils.printPlayerStates(stateMapConfig.stateMap(), getPlayerConnectionMap());
			stateMapConfig.setServer(server);
			stateMapConfig.addCallback(stateMap -> {
				ParagliderUtils.printPlayerStates(stateMap, getPlayerConnectionMap());
				ParagliderNetwork.get().syncStateMapToAll(server, stateMap);
			});
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			FabricPlayerStateMapConfig stateMapConfig = Objects.requireNonNull(this.stateMapConfig);
			stateMapConfig.setServer(null);
			stateMapConfig.removeCallbacks();
		});

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if(!success) return;
			ParagliderVillageStructures.addVillageStructures(server);
			ParagliderUtils.checkBargainRecipes(server);
		});

		AttackBlockCallback.EVENT.register((p, l, h, b, d) -> ParagliderEventHandler.beforeInteraction(p));
		AttackEntityCallback.EVENT.register((p, l, h, e, r) -> ParagliderEventHandler.beforeInteraction(p));
		UseBlockCallback.EVENT.register((p, l, h, r) -> ParagliderEventHandler.beforeInteraction(p));
		UseEntityCallback.EVENT.register((p, l, h, e, r) -> ParagliderEventHandler.beforeInteraction(p));
		UseItemCallback.EVENT.register((p, l, h) -> ParagliderEventHandler.beforeUseItem(p, h));
		ServerPlayerEvents.COPY_FROM.register(ParagliderEventHandler::onPlayerCopy);
		EntityTrackingEvents.START_TRACKING.register(ParagliderEventHandler::onStartTracking);
		ServerTickEvents.END_SERVER_TICK.register(s -> ParagliderEventHandler.afterServerTick());
		ServerPlayConnectionEvents.JOIN.register((l, p, s) -> ParagliderEventHandler.onLogin(l));

		ServerWorldEvents.LOAD.register((server, level) -> Wind.registerLevel(level));
		ServerWorldEvents.UNLOAD.register((server, level) -> Wind.unregisterLevel(level));
		ServerTickEvents.END_WORLD_TICK.register(WindUtils::updateWind);
		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
			Wind wind = Wind.of(level);
			if(wind!=null) wind.remove(chunk.getPos());
		});
		// chunk watch -> MixinChunkMap

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(ParagliderCommands.register()));
		LootTableEvents.MODIFY.register(FabricLootTable::modifyLootTables);

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FabricBargainTypeRegistry.get());

		ParagliderAPI.setMovementSupplier(p -> ((PlayerMovementAccess)p).paragliderPlayerMovement());
		ParagliderAPI.setStaminaSupplier(p -> ((PlayerMovementAccess)p).paragliderPlayerMovement().stamina());
		ParagliderAPI.setVesselContainerSupplier(p -> ((PlayerMovementAccess)p).paragliderPlayerMovement().vessels());

		var pair = PlayerStateMapLoader.loadStates();
		this.stateMapConfig = new FabricPlayerStateMapConfig(pair.getFirst());
		this.connectionMap = pair.getSecond();
		ParagliderAPI.setStaminaFactory(StaminaFactoryLoader.loadStaminaFactory());
	}

	@Override @NotNull public Cfg getConfig(){
		return config;
	}
	@Override @NotNull public DebugCfg getDebugConfig(){
		return commonConfig;
	}
	@Override @NotNull public FeatureCfg getFeatureConfig(){
		return commonConfig;
	}

	@Environment(EnvType.CLIENT)
	@Override @NotNull public ParagliderClientSettings getClientSettings(){
		return FabricParagliderClient.get().getClientSettings();
	}

	@Override @NotNull public FabricContents getContents(){
		return contents;
	}
	@Override @NotNull public ParagliderNetwork getNetwork(){
		return FabricParagliderNetwork.get();
	}
	@Override @NotNull public BargainTypeRegistry getBargainTypeRegistry(){
		return FabricBargainTypeRegistry.get();
	}
	@Override @NotNull public ParagliderPluginLoader getPluginLoader(){
		return FabricParagliderPluginLoader.get();
	}

	@Override @NotNull public PlayerStateMap getPlayerStateMap(){
		return switch(FabricLoader.getInstance().getEnvironmentType()){
			case CLIENT -> FabricParagliderClient.get().getPlayerStateMap();
			case SERVER -> getLocalPlayerStateMap();
		};
	}
	@Override @NotNull public PlayerStateMap getLocalPlayerStateMap(){
		if(stateMapConfig==null) throw new IllegalStateException("State map is not available yet");
		return this.stateMapConfig.stateMap();
	}
	@Override @NotNull public PlayerStateConnectionMap getPlayerConnectionMap(){
		if(connectionMap==null) throw new IllegalStateException("Connection map is not available yet");
		return this.connectionMap;
	}
	@Override @NotNull public PlayerStateMapConfig getPlayerStateMapConfig(){
		if(stateMapConfig==null) throw new IllegalStateException("State map is not available yet");
		return this.stateMapConfig;
	}

	@Environment(EnvType.CLIENT)
	@Override @NotNull public KeyMapping getParagliderSettingsKey(){
		return FabricParagliderClient.get().getParagliderSettingsKey();
	}
	@Environment(EnvType.CLIENT)
	@Override public void setSyncedPlayerStateMap(@Nullable PlayerStateMap stateMap){
		FabricParagliderClient.get().setSyncedPlayerStateMap(stateMap);
	}
}
