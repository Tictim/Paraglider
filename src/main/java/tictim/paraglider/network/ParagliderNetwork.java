package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.wind.WindChunk;

import java.util.List;

@NullMarked
public interface ParagliderNetwork {
	static ParagliderNetwork get() {
		return ParagliderMod.instance().getNetwork();
	}

	// movement

	void syncStateMap(ServerPlayer player, PlayerStateMap stateMap);
	void syncStateMapToAll(PlayerStateMap stateMap);

	void syncMovement(ServerPlayer player,
	                  Identifier state,
	                  double stamina,
	                  double extraStamina,
	                  boolean depleted,
	                  int recoveryDelay,
	                  double efficiency);

	void syncRemoteMovement(Entity entity, Identifier state);
	void syncRemoteMovement(Entity entity, ServerPlayer target, Identifier state);

	void syncVessels(ServerPlayer player,
	                 double stamina,
	                 double extraStamina,
	                 boolean depleted,
	                 int heartContainers,
	                 int staminaVessels);

	void syncCanUseParaglider(ServerPlayer player, boolean canUseParaglider, boolean canRideUpdraft);

	void setParaglidingToClient(ServerPlayer player, boolean paragliding);
	void setParaglidingToServer(boolean paragliding);

	void applyParagliderItemCooldown();

	// bargain

	void initBargain(BargainContext ctx, @Nullable Component initialDialog);

	void syncBargainCatalog(BargainContext ctx, List<BargainCatalog> catalog);

	void syncBargainLookAt(BargainContext ctx, @Nullable Vec3 lookAt);

	void displayBargainDialog(BargainContext ctx, Component dialog);

	void bargain(int sessionId, Identifier bargain);

	void bargainEndToClient(BargainContext ctx);
	void bargainEndToServer(int sessionId);

	// wind

	void syncWind(MinecraftServer server, LevelChunk chunk, WindChunk windChunk);
}
