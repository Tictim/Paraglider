package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderClientMod;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.client.screen.BargainScreen;
import tictim.paraglider.network.message.*;
import tictim.paraglider.wind.WindLevel;

@NullMarked
public final class ClientPacketHandler {
	private ClientPacketHandler() {}

	// movement

	public static void handleSyncPlayerStateMap(SyncPlayerStateMapMsg msg) {
		trace(Kind.MOVEMENT, msg);
		ParagliderClientMod.instance().setSyncedStateMap(msg.stateMap());
	}

	public static void handleSyncMovement(SyncMovementMsg msg) {
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		if (Movement.get(mc.player) instanceof SyncMovementHandle smh) {
			smh.syncMovement(msg.state(), msg.recoveryDelay(), msg.efficiency());
		}
		Stamina.get(mc.player).syncProperties(msg.stamina(), msg.extraStamina(), msg.depleted());
	}

	public static void handleSyncRemoteMovement(SyncRemoteMovementMsg msg) {
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Player player = mc.level.getPlayerByUUID(msg.entityId());
		if (player == null) return;
		if (Movement.get(player) instanceof SyncMovementHandle smh) {
			smh.syncRemoteMovement(msg.state());
		}
	}

	public static void handleSyncVessel(SyncVesselMsg msg) {
		trace(Kind.VESSEL, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		VesselContainer vessels = VesselContainer.get(mc.player);
		vessels.setHeartContainer(msg.heartContainers(), false, false);
		vessels.setStaminaVessel(msg.staminaVessels(), false, false);
		Stamina.get(mc.player).syncProperties(msg.stamina(), msg.extraStamina(), msg.depleted());
	}

	public static void handleSyncCanUseParaglider(SyncCanUseParagliderMsg msg) {
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		if (Movement.get(mc.player) instanceof SyncCanUseParagliderHandle h) {
			h.syncCanUseParaglider(msg.canUseParaglider(), msg.canRideUpdraft());
		}
	}

	public static void handleSetParagliding(SetParaglidingMsg msg) {
		trace(Kind.MOVEMENT, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		if (Movement.get(mc.player) instanceof SyncClientParaglidingHandle h) {
			h.syncClientParagliding(msg.paragliding());
		}
	}

	// bargain

	public static void handleBargainInit(BargainInitMsg msg) {
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new BargainScreen(msg.sessionId(), msg.catalog(), msg.lookAt().orElse(null), msg.dialog().orElse(null)));
	}

	public static void handleSyncCatalog(SyncCatalogMsg msg) {
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof BargainScreen bargainScreen && bargainScreen.sessionId == msg.sessionId()) {
			bargainScreen.setCatalog(msg.catalog());
		}
	}

	public static void handleSyncLookAt(SyncLookAtMsg msg) {
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof BargainScreen bargainScreen && bargainScreen.sessionId == msg.sessionId()) {
			bargainScreen.setLookAt(msg.lookAt().orElse(null));
		}
	}

	public static void handleBargainDialog(BargainDialogMsg msg) {
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof BargainScreen bargainScreen && bargainScreen.sessionId == msg.sessionId()) {
			bargainScreen.setDialog(msg.dialog());
		}
	}

	public static void handleBargainEnd(BargainEndMsg msg) {
		trace(Kind.BARGAIN, msg);
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof BargainScreen bargainScreen && bargainScreen.sessionId == msg.sessionId()) {
			mc.setScreen(null);
		}
	}

	// wind

	public static void handleSyncWind(SyncWindMsg msg) {
		trace(Kind.WIND, msg);
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		WindLevel wind = WindLevel.of(level);
		if (wind != null) wind.putChunk(msg.windChunk());
	}

	private static void trace(Kind kind, CustomPacketPayload msg) {
		if (kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Received {} from server", msg);
	}
}
