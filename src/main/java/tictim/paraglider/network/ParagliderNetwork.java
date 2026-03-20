package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.bargain.BargainContext;
import tictim.paraglider.impl.movement.PlayerStateMap;
import tictim.paraglider.wind.WindChunk;

import java.util.List;

public interface ParagliderNetwork {
	static @NotNull ParagliderNetwork get() {
		return ParagliderMod.instance().getNetwork();
	}

	// movement

	void syncStateMap(@NotNull ServerPlayer player, @NotNull PlayerStateMap stateMap);
	void syncStateMapToAll(@NotNull PlayerStateMap stateMap);

	void syncMovement(@NotNull ServerPlayer player,
	                  @NotNull Identifier state,
	                  double stamina,
	                  double extraStamina,
	                  boolean depleted,
	                  int recoveryDelay,
	                  double efficiency);

	void syncRemoteMovement(@NotNull Entity entity, @NotNull Identifier state);
	void syncRemoteMovement(@NotNull Entity entity, @NotNull ServerPlayer target, @NotNull Identifier state);

	void syncVessels(@NotNull ServerPlayer player,
	                 double stamina,
	                 double extraStamina,
	                 boolean depleted,
	                 int heartContainers,
	                 int staminaVessels);

	void syncCanUseParaglider(@NotNull ServerPlayer player, boolean canUseParaglider, boolean canRideUpdraft);

	void setParaglidingToClient(@NotNull ServerPlayer player, boolean paragliding);
	void setParaglidingToServer(boolean paragliding);

	// bargain

	void initBargain(@NotNull BargainContext ctx, @Nullable Component initialDialog);

	void syncBargainCatalog(@NotNull BargainContext ctx, @NotNull List<BargainCatalog> catalog);

	void syncBargainLookAt(@NotNull BargainContext ctx, @Nullable Vec3 lookAt);

	void displayBargainDialog(@NotNull BargainContext ctx, @NotNull Component dialog);

	void bargain(int sessionId, @NotNull Identifier bargain);

	void bargainEndToClient(@NotNull BargainContext ctx);
	void bargainEndToServer(int sessionId);

	// wind

	void syncWind(@NotNull MinecraftServer server, @NotNull LevelChunk chunk, @NotNull WindChunk windChunk);
}
