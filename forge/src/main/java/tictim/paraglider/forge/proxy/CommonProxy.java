package tictim.paraglider.forge.proxy;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.command.ParagliderCommands;
import tictim.paraglider.config.PlayerStateMapConfig;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderVillageStructures;
import tictim.paraglider.forge.ForgeParagliderNetwork;
import tictim.paraglider.forge.capability.PlayerMovementProvider;
import tictim.paraglider.forge.config.ForgePlayerStateMapConfig;
import tictim.paraglider.impl.movement.*;
import tictim.paraglider.impl.stamina.NullStamina;
import tictim.paraglider.impl.stamina.StaminaFactoryLoader;
import tictim.paraglider.impl.vessel.NullVesselContainer;
import tictim.paraglider.network.ParagliderNetwork;

public class CommonProxy{
	private final PlayerStateMapConfig stateMapConfig;
	private final PlayerStateConnectionMap connectionMap;

	{
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(() -> {
			CauldronInteraction.WATER.put(Contents.get().paraglider(), CauldronInteraction.DYED_ITEM);
			CauldronInteraction.WATER.put(Contents.get().dekuLeaf(), CauldronInteraction.DYED_ITEM);
		}));
		eventBus.addListener((RegisterCapabilitiesEvent e) -> e.register(PlayerMovement.class));

		ParagliderAPI.setMovementSupplier(p -> {
			PlayerMovement m = PlayerMovementProvider.of(p);
			return m==null ? NullMovement.get() : m;
		});
		ParagliderAPI.setStaminaSupplier(p -> {
			PlayerMovement m = PlayerMovementProvider.of(p);
			return m==null ? NullStamina.get() : m.stamina();
		});
		ParagliderAPI.setVesselContainerSupplier(p -> {
			PlayerMovement m = PlayerMovementProvider.of(p);
			return m==null ? NullVesselContainer.get() : m.vessels();
		});

		var pair = PlayerStateMapLoader.loadStates();
		this.stateMapConfig = new ForgePlayerStateMapConfig(pair.getFirst());
		this.connectionMap = pair.getSecond();
		ParagliderAPI.setStaminaFactory(StaminaFactoryLoader.loadStaminaFactory());

		MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> e.getDispatcher().register(ParagliderCommands.register()));
		// OnDatapackSyncEvent without player is called on datapack reload
		MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent e) -> {
			MinecraftServer server = e.getPlayerList().getServer();
			ParagliderUtils.checkBargainRecipes(server);
			ParagliderVillageStructures.addVillageStructures(server);
		});
		MinecraftForge.EVENT_BUS.addListener((ServerStoppingEvent e) -> this.stateMapConfig.removeCallbacks());

		ForgeParagliderNetwork.init();
	}

	protected void onServerAboutToStart(ServerAboutToStartEvent event){
		MinecraftServer server = event.getServer();
		ParagliderVillageStructures.addVillageStructures(server);
		PlayerStateMapConfig stateMapConfig = this.stateMapConfig;
		stateMapConfig.removeCallbacks();
		stateMapConfig.reload();
		ParagliderUtils.printPlayerStates(stateMapConfig.stateMap(), getConnectionMap());
		stateMapConfig.addCallback(stateMap -> {
			ParagliderUtils.printPlayerStates(stateMap, getConnectionMap());
			ParagliderNetwork.get().syncStateMapToAll(server, stateMap);
		});
		ParagliderUtils.checkBargainRecipes(server);
	}

	@NotNull public ParagliderClientSettings getClientSettings(){
		throw new IllegalStateException("Trying to access client settings in server environment");
	}

	@NotNull public PlayerStateMap getStateMap(){
		return getLocalStateMap();
	}
	@NotNull public PlayerStateMap getLocalStateMap(){
		return stateMapConfig.stateMap();
	}
	@NotNull public PlayerStateConnectionMap getConnectionMap(){
		return connectionMap;
	}
	@NotNull public PlayerStateMapConfig getStateMapConfig(){
		return stateMapConfig;
	}

	public void setSyncedStateMap(@Nullable PlayerStateMap stateMap){
		throw new IllegalStateException("Trying to access client side value in server environment");
	}

	@OnlyIn(Dist.CLIENT)
	@NotNull public KeyMapping getParagliderSettingsKey(){
		throw new AssertionError();
	}
}
