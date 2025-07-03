package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.network.message.*;
import tictim.paraglider.wind.WindChunk;

import java.util.List;

@SuppressWarnings("SameParameterValue")
public class ParagliderNetworkImpl implements ParagliderNetwork {
	public static final String NETVERSION = "3";

	public ParagliderNetworkImpl(IEventBus eventBus) {
		eventBus.addListener(this::register);
	}

	private void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar reg = event.registrar(NETVERSION);
		reg.commonToClient(SyncPlayerStateMapMsg.TYPE, SyncPlayerStateMapMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncPlayerStateMap(msg));

		reg.playToClient(SyncMovementMsg.TYPE, SyncMovementMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncMovement(msg));
		reg.playToClient(SyncRemoteMovementMsg.TYPE, SyncRemoteMovementMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncRemoteMovement(msg));
		reg.playToClient(SyncVesselMsg.TYPE, SyncVesselMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncVessel(msg));

		reg.playToClient(BargainInitMsg.TYPE, BargainInitMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleBargainInit(msg));
		reg.playToClient(SyncCatalogMsg.TYPE, SyncCatalogMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncCatalog(msg));
		reg.playToClient(SyncLookAtMsg.TYPE, SyncLookAtMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncLookAt(msg));
		reg.playToClient(BargainDialogMsg.TYPE, BargainDialogMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleBargainDialog(msg));

		reg.playToClient(SyncWindMsg.TYPE, SyncWindMsg.CODEC,
				(msg, ctx) -> ClientPacketHandler.handleSyncWind(msg));

		reg.playToServer(BargainMsg.TYPE, BargainMsg.CODEC, ServerPacketHandler::handleBargain);

		reg.playBidirectional(BargainEndMsg.TYPE, BargainEndMsg.CODEC, new DirectionalPayloadHandler<>(
				(msg, ctx) -> ClientPacketHandler.handleBargainEnd(msg),
				ServerPacketHandler::handleBargainEnd));
	}

	@Override public void syncStateMap(@NotNull ServerPlayer player, @NotNull PlayerStateMap stateMap) {
		SyncPlayerStateMapMsg msg = new SyncPlayerStateMapMsg(stateMap);
		traceSendToPlayer(Kind.MOVEMENT, player, msg);
		PacketDistributor.sendToPlayer(player, msg);
	}

	@Override public void syncStateMapToAll(@NotNull MinecraftServer server, @NotNull PlayerStateMap stateMap) {
		SyncPlayerStateMapMsg msg = new SyncPlayerStateMapMsg(stateMap);
		traceSendToAll(Kind.MOVEMENT, msg);
		PacketDistributor.sendToAllPlayers(msg);
	}

	@Override public void syncMovement(@NotNull ServerPlayer player, @NotNull ResourceLocation state,
	                                   double stamina, double extraStamina, boolean depleted,
	                                   int recoveryDelay, double efficiency) {
		SyncMovementMsg msg = new SyncMovementMsg(state, stamina, extraStamina, depleted, recoveryDelay, efficiency);
		traceSendToPlayer(Kind.MOVEMENT, player, msg);
		PacketDistributor.sendToPlayer(player, msg);
		syncRemoteMovement(player.server, player, state);
	}

	@Override public void syncRemoteMovement(@NotNull MinecraftServer server, @NotNull Entity entity, @NotNull ResourceLocation state) {
		SyncRemoteMovementMsg msg = new SyncRemoteMovementMsg(entity.getUUID(), state);
		traceSendToTracking(Kind.MOVEMENT, entity, msg);
		PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
	}

	@Override public void syncRemoteMovement(@NotNull Entity entity, @NotNull ServerPlayer target, @NotNull ResourceLocation state) {
		SyncRemoteMovementMsg msg = new SyncRemoteMovementMsg(entity.getUUID(), state);
		traceSendToPlayer(Kind.MOVEMENT, target, msg);
		PacketDistributor.sendToPlayer(target, msg);
	}

	@Override public void syncVessels(@NotNull ServerPlayer player,
	                                  double stamina, double extraStamina, boolean depleted,
	                                  int heartContainers, int staminaVessels) {
		SyncVesselMsg msg = new SyncVesselMsg(stamina, extraStamina, depleted, heartContainers, staminaVessels);
		traceSendToPlayer(Kind.VESSEL, player, msg);
		PacketDistributor.sendToPlayer(player, msg);
	}

	@Override public void initBargain(@NotNull BargainContext ctx,
	                                  @Nullable Component dialog) {
		BargainInitMsg msg = new BargainInitMsg(ctx.sessionId(), ctx.makeCatalog(), ctx.lookAt(), dialog);
		traceSendToPlayer(Kind.BARGAIN, ctx.player(), msg);
		PacketDistributor.sendToPlayer(ctx.player(), msg);
	}

	@Override public void syncBargainCatalog(@NotNull BargainContext ctx, @NotNull List<BargainCatalog> catalog) {
		SyncCatalogMsg msg = new SyncCatalogMsg(ctx.sessionId(), catalog);
		traceSendToPlayer(Kind.BARGAIN, ctx.player(), msg);
		PacketDistributor.sendToPlayer(ctx.player(), msg);
	}

	@Override public void syncBargainLookAt(@NotNull BargainContext ctx, @Nullable Vec3 lookAt) {
		SyncLookAtMsg msg = new SyncLookAtMsg(ctx.sessionId(), lookAt);
		traceSendToPlayer(Kind.BARGAIN, ctx.player(), msg);
		PacketDistributor.sendToPlayer(ctx.player(), msg);
	}

	@Override public void displayBargainDialog(@NotNull BargainContext ctx, @NotNull Component dialog) {
		BargainDialogMsg msg = new BargainDialogMsg(ctx.sessionId(), dialog);
		traceSendToPlayer(Kind.BARGAIN, ctx.player(), msg);
		PacketDistributor.sendToPlayer(ctx.player(), msg);
	}

	@Override public void bargain(int sessionId, @NotNull ResourceLocation bargain) {
		BargainMsg msg = new BargainMsg(sessionId, bargain);
		traceSendToServer(Kind.BARGAIN, msg);
		PacketDistributor.sendToServer(msg);
	}

	@Override public void bargainEndToClient(@NotNull BargainContext ctx) {
		BargainEndMsg msg = new BargainEndMsg(ctx.sessionId());
		traceSendToPlayer(Kind.BARGAIN, ctx.player(), msg);
		PacketDistributor.sendToPlayer(ctx.player(), msg);
	}

	@Override public void bargainEndToServer(int sessionId) {
		BargainEndMsg msg = new BargainEndMsg(sessionId);
		traceSendToServer(Kind.BARGAIN, msg);
		PacketDistributor.sendToServer(msg);
	}

	@Override
	public void syncWind(@NotNull MinecraftServer server, @NotNull LevelChunk chunk, @NotNull WindChunk windChunk) {
		SyncWindMsg msg = new SyncWindMsg(windChunk);
		traceSendToTracking(Kind.WIND, chunk, msg);

		if (chunk.getLevel() instanceof ServerLevel serverLevel) {
			PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunk.getPos(), msg);
		} else {
			ParagliderMod.LOGGER.warn("Failed to send packet {}, not a server level", msg);
		}
	}

	protected static void traceSendToAll(@NotNull Kind kind, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Dispatching {} to clients", msg);
	}

	protected static void traceSendToPlayer(@NotNull Kind kind, @NotNull ServerPlayer player, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Dispatching {} to {}", msg, player);
	}

	protected static void traceSendToTracking(@NotNull Kind kind, @NotNull Entity entity, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled())
			ParagliderMod.LOGGER.debug("Dispatching {} to clients tracking entity {}", msg, entity);
	}

	protected static void traceSendToTracking(@NotNull Kind kind, @NotNull LevelChunk chunk, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled())
			ParagliderMod.LOGGER.debug("Dispatching {} to clients tracking chunk {}", msg, chunk);
	}

	protected static void traceSendToServer(@NotNull Kind kind, @NotNull CustomPacketPayload msg) {
		if (kind.isTraceEnabled()) ParagliderMod.LOGGER.debug("Dispatching {} to server", msg);
	}
}
