package tictim.paraglider.wind;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.List;

public final class WindUtils {
	private WindUtils() {}

	public static void updateWind(@NotNull Level level) {
		Wind wind = Wind.of(level);
		if (wind == null) return;

		long gameTime = level.getGameTime();
		if (gameTime % 4 == 0) {
			List<? extends Player> players = level.players();
			for (Player player : players) {
				if (player.getMainHandItem().is(ParagliderTags.PARAGLIDERS)) {
					wind.placeAround(player);
				}
			}
		}

		wind.checkPlacedWind(level);

		if (level instanceof ServerLevel serverLevel) {
			for (var it = wind.dirtyWindChunks().iterator(); it.hasNext(); ) {
				long chunkPos = it.nextLong();
				WindChunk windChunk = wind.getChunk(chunkPos);
				if (windChunk == null) continue; // ???
				LevelChunk chunk = level.getChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));
				ParagliderNetwork.get().syncWind(serverLevel.getServer(), chunk, windChunk);
			}
		}

		wind.dirtyWindChunks().clear();
	}
}
