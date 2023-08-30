package tictim.paraglider.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Map;

public interface ParagliderNetwork{
	@NotNull static ParagliderNetwork get(){
		return ParagliderMod.instance().getNetwork();
	}

	// movement

	void syncStateMap(@NotNull ServerPlayer player, @NotNull PlayerStateMap stateMap);
	void syncStateMapToAll(@NotNull MinecraftServer server, @NotNull PlayerStateMap stateMap);

	void syncMovement(@NotNull ServerPlayer player,
	                  @NotNull ResourceLocation state,
	                  int stamina,
	                  boolean depleted,
	                  int recoveryDelay);

	void syncRemoteMovement(@NotNull MinecraftServer server, @NotNull Entity entity, @NotNull ResourceLocation state);
	void syncRemoteMovement(@NotNull Entity entity, @NotNull ServerPlayer target, @NotNull ResourceLocation state);

	void syncVessels(@NotNull ServerPlayer player,
	                 int stamina,
	                 int heartContainers,
	                 int staminaVessels);

	// bargain

	void initBargain(@NotNull BargainContext ctx, @Nullable Component initialDialog);

	void syncBargainCatalog(@NotNull BargainContext ctx, @NotNull Map<ResourceLocation, BargainCatalog> catalog);

	void syncBargainLookAt(@NotNull BargainContext ctx, @Nullable Vec3 lookAt);

	void displayBargainDialog(@NotNull BargainContext ctx, @NotNull Component dialog);

	void bargain(int sessionId, @NotNull ResourceLocation bargain);

	void bargainEndToClient(@NotNull BargainContext ctx);
	void bargainEndToServer(int sessionId);

	// wind

	void syncWind(@NotNull MinecraftServer server, @NotNull LevelChunk chunk, @NotNull WindChunk windChunk);
}
