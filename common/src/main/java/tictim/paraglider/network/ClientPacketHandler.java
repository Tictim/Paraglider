package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.client.screen.BargainScreen;
import tictim.paraglider.network.message.BargainDialogMsg;
import tictim.paraglider.network.message.BargainEndMsg;
import tictim.paraglider.network.message.BargainInitMsg;
import tictim.paraglider.network.message.Msg;
import tictim.paraglider.network.message.SyncCatalogMsg;
import tictim.paraglider.network.message.SyncLookAtMsg;
import tictim.paraglider.network.message.SyncMovementMsg;
import tictim.paraglider.network.message.SyncPlayerStateMapMsg;
import tictim.paraglider.network.message.SyncRemoteMovementMsg;
import tictim.paraglider.network.message.SyncVesselMsg;
import tictim.paraglider.network.message.SyncWindMsg;
import tictim.paraglider.wind.Wind;

public final class ClientPacketHandler{
	private ClientPacketHandler(){}

	// movement

	public static void handleSyncPlayerStateMap(SyncPlayerStateMapMsg msg){
		trace(Kind.MOVEMENT, msg);
		ParagliderMod.instance().setSyncedPlayerStateMap(msg.stateMap());
	}

	public static void handleSyncMovement(SyncMovementMsg msg){
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.player==null) return;
		if(Movement.get(mc.player) instanceof SyncMovementHandle smh){
			smh.syncMovement(msg.state(), msg.stamina(), msg.depleted(), msg.recoveryDelay(), msg.reductionRate());
		}
	}

	public static void handleSyncRemoteMovement(SyncRemoteMovementMsg msg){
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.level==null) return;
		Player player = mc.level.getPlayerByUUID(msg.entityId());
		if(player==null) return;
		if(Movement.get(player) instanceof SyncMovementHandle smh){
			smh.syncRemoteMovement(msg.state());
		}
	}

	public static void handleSyncVessel(SyncVesselMsg msg){
		trace(Kind.VESSEL, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.player==null) return;
		VesselContainer vessels = VesselContainer.get(mc.player);
		vessels.setHeartContainer(msg.heartContainers(), false, false);
		vessels.setStaminaVessel(msg.staminaVessels(), false, false);
		Stamina.get(mc.player).setStamina(msg.stamina());
	}

	// bargain

	public static void handleBargainInit(BargainInitMsg msg){
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new BargainScreen(msg.sessionId(), msg.catalog(), msg.lookAt(), msg.dialog()));
	}

	public static void handleSyncCatalog(SyncCatalogMsg msg){
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof BargainScreen bargainScreen&&bargainScreen.sessionId==msg.sessionId()){
			bargainScreen.setCatalog(msg.catalog());
		}
	}

	public static void handleSyncLookAt(SyncLookAtMsg msg){
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof BargainScreen bargainScreen&&bargainScreen.sessionId==msg.sessionId()){
			bargainScreen.setLookAt(msg.lookAt());
		}
	}

	public static void handleBargainDialog(BargainDialogMsg msg){
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof BargainScreen bargainScreen&&bargainScreen.sessionId==msg.sessionId()){
			bargainScreen.setDialog(msg.dialog());
		}
	}

	public static void handleBargainEnd(BargainEndMsg msg){
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof BargainScreen bargainScreen&&bargainScreen.sessionId==msg.sessionId()){
			mc.setScreen(null);
		}
	}

	// wind

	public static void handleSyncWind(SyncWindMsg msg){
		trace(Kind.WIND, msg);
		ClientLevel world = Minecraft.getInstance().level;
		if(world==null) return;
		Wind wind = Wind.of(world);
		if(wind!=null) wind.put(msg.windChunk());
	}

	private static void trace(@NotNull Kind kind, @NotNull Msg msg){
		if(kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Received {} from server", msg);
	}
}
