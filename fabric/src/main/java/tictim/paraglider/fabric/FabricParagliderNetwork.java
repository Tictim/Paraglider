package tictim.paraglider.fabric;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.network.ClientPacketHandler;
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

import java.util.Collection;

import static tictim.paraglider.api.ParagliderAPI.id;
import static tictim.paraglider.fabric.NetworkIDs.*;

public final class FabricParagliderNetwork extends ParagliderNetworkBase{
	private FabricParagliderNetwork(){}

	private static final FabricParagliderNetwork instance = new FabricParagliderNetwork();

	public static FabricParagliderNetwork get(){
		return instance;
	}

	// just for loading the class
	public static void init(){
		ServerPlayNetworking.registerGlobalReceiver(BARGAIN_END, (server, player, handler, buf, responseSender) -> {
			var msg = BargainEndMsg.read(buf);
			server.execute(() -> ServerPacketHandler.handleBargainEnd(player, msg));
		});
		ServerPlayNetworking.registerGlobalReceiver(BARGAIN, (server, player, handler, buf, responseSender) -> {
			var msg = BargainMsg.read(buf);
			server.execute(() -> ServerPacketHandler.handleBargain(player, msg));
		});
	}

	public static void clientInit(){
		ClientPlayNetworking.registerGlobalReceiver(BARGAIN_DIALOG, (client, handler, buf, responseSender) -> {
			var msg = BargainDialogMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleBargainDialog(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(BARGAIN_END, (client, handler, buf, responseSender) -> {
			var msg = BargainEndMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleBargainEnd(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(BARGAIN_INIT, (client, handler, buf, responseSender) -> {
			var msg = BargainInitMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleBargainInit(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_CATALOG, (client, handler, buf, responseSender) -> {
			var msg = SyncCatalogMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncCatalog(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOK_AT, (client, handler, buf, responseSender) -> {
			var msg = SyncLookAtMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncLookAt(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_MOVEMENT, (client, handler, buf, responseSender) -> {
			var msg = SyncMovementMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncMovement(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_PLAYER_STATE_MAP, (client, handler, buf, responseSender) -> {
			var msg = SyncPlayerStateMapMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncPlayerStateMap(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_REMOTE_MOVEMENT, (client, handler, buf, responseSender) -> {
			var msg = SyncRemoteMovementMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncRemoteMovement(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_VESSEL, (client, handler, buf, responseSender) -> {
			var msg = SyncVesselMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncVessel(msg));
		});
		ClientPlayNetworking.registerGlobalReceiver(SYNC_WIND, (client, handler, buf, responseSender) -> {
			var msg = SyncWindMsg.read(buf);
			client.execute(() -> ClientPacketHandler.handleSyncWind(msg));
		});
	}

	@Override protected void sendToAll(@NotNull MinecraftServer server, @NotNull Msg msg){
		Collection<ServerPlayer> players = PlayerLookup.all(server);
		if(players.isEmpty()) return;
		var pair = prepare(msg);
		for(ServerPlayer player : players) ServerPlayNetworking.send(player, pair.getFirst(), pair.getSecond());
	}
	@Override protected void sendToPlayer(@NotNull ServerPlayer player, @NotNull Msg msg){
		var pair = prepare(msg);
		ServerPlayNetworking.send(player, pair.getFirst(), pair.getSecond());
	}
	@Override protected void sendToTracking(@NotNull MinecraftServer server, @NotNull Entity entity, @NotNull Msg msg){
		Collection<ServerPlayer> players = PlayerLookup.tracking(entity);
		if(players.isEmpty()) return;
		var pair = prepare(msg);
		for(ServerPlayer player : players){
			if(player==entity) continue; // Do not send to itself
			ServerPlayNetworking.send(player, pair.getFirst(), pair.getSecond());
		}
	}
	@Override protected void sendToTracking(@NotNull MinecraftServer server, @NotNull LevelChunk chunk, @NotNull Msg msg){
		if(!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;
		Collection<ServerPlayer> players = PlayerLookup.tracking(serverLevel, chunk.getPos());
		if(players.isEmpty()) return;
		var pair = prepare(msg);
		for(ServerPlayer player : players) ServerPlayNetworking.send(player, pair.getFirst(), pair.getSecond());
	}
	@Override protected void sendToServer(@NotNull Msg msg){
		var pair = prepare(msg);
		ClientPlayNetworking.send(pair.getFirst(), pair.getSecond());
	}

	@NotNull private static Pair<@NotNull ResourceLocation, @NotNull FriendlyByteBuf> prepare(@NotNull Msg msg){
		ResourceLocation id;

		// God why is pattern matching switch cases still in preview fuck you java fuck you mojang
		if(msg instanceof BargainDialogMsg) id = BARGAIN_DIALOG;
		else if(msg instanceof BargainEndMsg) id = BARGAIN_END;
		else if(msg instanceof BargainInitMsg) id = BARGAIN_INIT;
		else if(msg instanceof BargainMsg) id = BARGAIN;
		else if(msg instanceof SyncCatalogMsg) id = SYNC_CATALOG;
		else if(msg instanceof SyncLookAtMsg) id = SYNC_LOOK_AT;
		else if(msg instanceof SyncMovementMsg) id = SYNC_MOVEMENT;
		else if(msg instanceof SyncPlayerStateMapMsg) id = SYNC_PLAYER_STATE_MAP;
		else if(msg instanceof SyncRemoteMovementMsg) id = SYNC_REMOTE_MOVEMENT;
		else if(msg instanceof SyncVesselMsg) id = SYNC_VESSEL;
		else if(msg instanceof SyncWindMsg) id = SYNC_WIND;
		else throw new IllegalStateException("@Tictim <<< laugh at this user!!!");

		FriendlyByteBuf buf = PacketByteBufs.create();
		msg.write(buf);

		return Pair.of(id, buf);
	}
}

interface NetworkIDs{
	ResourceLocation BARGAIN_DIALOG = id("bargain_dialog");
	ResourceLocation BARGAIN_END = id("bargain_end");
	ResourceLocation BARGAIN_INIT = id("bargain_init");
	ResourceLocation BARGAIN = id("bargain");
	ResourceLocation SYNC_CATALOG = id("sync_catalog");
	ResourceLocation SYNC_LOOK_AT = id("sync_look_at");
	ResourceLocation SYNC_MOVEMENT = id("sync_movement");
	ResourceLocation SYNC_PLAYER_STATE_MAP = id("sync_player_state_map");
	ResourceLocation SYNC_REMOTE_MOVEMENT = id("sync_remote_movement");
	ResourceLocation SYNC_VESSEL = id("sync_vessel");
	ResourceLocation SYNC_WIND = id("sync_wind");
}