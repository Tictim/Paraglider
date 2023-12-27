package tictim.paraglider.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.network.ClientPacketHandler;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.network.ParagliderNetworkBase;
import tictim.paraglider.network.ServerPacketHandler;
import tictim.paraglider.network.message.BargainDialogMsg;
import tictim.paraglider.network.message.BargainEndMsg;
import tictim.paraglider.network.message.BargainInitMsg;
import tictim.paraglider.network.message.BargainMsg;
import tictim.paraglider.network.message.Msg;
import tictim.paraglider.network.message.SyncCatalogMsg;
import tictim.paraglider.network.message.SyncLookAtMsg;
import tictim.paraglider.network.message.SyncMovementMsg;
import tictim.paraglider.network.message.SyncPlayerStateMapMsg;
import tictim.paraglider.network.message.SyncRemoteMovementMsg;
import tictim.paraglider.network.message.SyncVesselMsg;
import tictim.paraglider.network.message.SyncWindMsg;

public final class ForgeParagliderNetwork extends ParagliderNetworkBase{
	private static final ForgeParagliderNetwork instance = new ForgeParagliderNetwork();

	public static final int NETVERSION = 2;

	@NotNull public static ParagliderNetwork get(){
		return instance;
	}

	// just for loading the class
	public static void init(){}

	private final SimpleChannel net = ChannelBuilder
		.named(ParagliderAPI.id("master"))
		.networkProtocolVersion(NETVERSION)
		.acceptedVersions((status, version) -> version == NETVERSION)
		.simpleChannel()

		.messageBuilder(SyncPlayerStateMapMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncPlayerStateMapMsg::write)
		.decoder(SyncPlayerStateMapMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncPlayerStateMap)
		.add()

		.messageBuilder(SyncMovementMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncMovementMsg::write)
		.decoder(SyncMovementMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncMovement)
		.add()

		.messageBuilder(SyncRemoteMovementMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncRemoteMovementMsg::write)
		.decoder(SyncRemoteMovementMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncRemoteMovement)
		.add()

		.messageBuilder(SyncVesselMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncVesselMsg::write)
		.decoder(SyncVesselMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncVessel)
		.add()

		.messageBuilder(BargainInitMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(BargainInitMsg::write)
		.decoder(BargainInitMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleBargainInit)
		.add()

		.messageBuilder(SyncCatalogMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncCatalogMsg::write)
		.decoder(SyncCatalogMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncCatalog)
		.add()

		.messageBuilder(SyncLookAtMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncLookAtMsg::write)
		.decoder(SyncLookAtMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncLookAt)
		.add()

		.messageBuilder(BargainDialogMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(BargainDialogMsg::write)
		.decoder(BargainDialogMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleBargainDialog)
		.add()

		.messageBuilder(BargainMsg.class, NetworkDirection.PLAY_TO_SERVER)
		.encoder(BargainMsg::write)
		.decoder(BargainMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleBargain)
		.add()

		.messageBuilder(BargainEndMsg.class)
		.encoder(BargainEndMsg::write)
		.decoder(BargainEndMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleBargainEnd)
		.add()

		.messageBuilder(SyncWindMsg.class, NetworkDirection.PLAY_TO_CLIENT)
		.encoder(SyncWindMsg::write)
		.decoder(SyncWindMsg::read)
		.consumerNetworkThread(ForgeParagliderNetwork::handleSyncWind)
		.add();


	private static void handleSyncPlayerStateMap(SyncPlayerStateMapMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncPlayerStateMap(msg));
	}

	private static void handleSyncMovement(SyncMovementMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncMovement(msg));
	}

	private static void handleSyncRemoteMovement(SyncRemoteMovementMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncRemoteMovement(msg));
	}

	private static void handleSyncVessel(SyncVesselMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncVessel(msg));
	}

	private static void handleBargainInit(BargainInitMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleBargainInit(msg));
	}

	private static void handleSyncCatalog(SyncCatalogMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncCatalog(msg));
	}

	private static void handleSyncLookAt(SyncLookAtMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncLookAt(msg));
	}

	private static void handleBargainDialog(BargainDialogMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleBargainDialog(msg));
	}

	private static void handleBargain(BargainMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			if(player==null) return; // wrong side
			ServerPacketHandler.handleBargain(player, msg);
		});
	}

	private static void handleBargainEnd(BargainEndMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> {
			switch(ctx.getDirection().getReceptionSide()){
				case CLIENT -> ClientPacketHandler.handleBargainEnd(msg);
				case SERVER -> {
					ServerPlayer player = ctx.getSender();
					if(player!=null) ServerPacketHandler.handleBargainEnd(player, msg);
				}
			}
		});
	}

	private static void handleSyncWind(SyncWindMsg msg, CustomPayloadEvent.Context ctx) {
		ctx.setPacketHandled(true);
		ctx.enqueueWork(() -> ClientPacketHandler.handleSyncWind(msg));
	}

	@Override protected void sendToAll(@NotNull MinecraftServer server, @NotNull Msg msg){
		net.send(msg, PacketDistributor.ALL.noArg());
	}
	@Override protected void sendToPlayer(@NotNull ServerPlayer player, @NotNull Msg msg){
		net.send(msg, PacketDistributor.PLAYER.with(player));
	}
	@Override protected void sendToTracking(@NotNull MinecraftServer server, @NotNull Entity entity, @NotNull Msg msg){
		net.send(msg, PacketDistributor.TRACKING_ENTITY.with(entity));
	}
	@Override protected void sendToTracking(@NotNull MinecraftServer server, @NotNull LevelChunk chunk, @NotNull Msg msg){
		net.send(msg, PacketDistributor.TRACKING_CHUNK.with(chunk));
	}
	@Override protected void sendToServer(@NotNull Msg msg){
		net.send(msg, Minecraft.getInstance().getConnection().getConnection());
	}
}
