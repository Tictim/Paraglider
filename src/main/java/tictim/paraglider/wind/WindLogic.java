package tictim.paraglider.wind;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.List;

@NullMarked
public final class WindLogic {
	private WindLogic() {}

	private static final int XZ_RAD_HALF = 4;
	private static final int GROUND_Y_MIN = -2;
	private static final int GROUND_Y_MAX = 4;
	private static final int PARAGLIDING_Y_MAX = 1;

	private static final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

	public static void updateWind(Level level) {
		WindLevel wind = WindLevel.of(level);
		if (wind == null) return;

		long gameTime = level.getGameTime();
		if (gameTime % 4 == 0) {
			List<? extends Player> players = level.players();
			for (Player player : players) {
				if (player.getMainHandItem().is(ParagliderTags.PARAGLIDERS)) {
					placeAround(wind, player);
				}
			}
		}

		checkPlacedWind(wind, level);

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

	/**
	 * Scans blocks around player and update wind chunks. Scan range is predefined.
	 */
	private static void placeAround(WindLevel wind, Player player) {
		int x = Mth.floor(player.getX());
		int y = Mth.floor(player.getY());
		int z = Mth.floor(player.getZ());

		place(wind, player.level(),
				x - XZ_RAD_HALF, y + (player.onGround() ? GROUND_Y_MIN : -WindSourceRegistry.get().maxWindHeight() - 1), z - XZ_RAD_HALF,
				x + XZ_RAD_HALF, y + (player.onGround() ? GROUND_Y_MAX : PARAGLIDING_Y_MAX), z + XZ_RAD_HALF);
	}

	/**
	 * Scans blocks in range and update wind chunks.
	 */
	private static void place(WindLevel wind, Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!level.getChunkSource().hasChunk(
						SectionPos.blockToSectionCoord(x),
						SectionPos.blockToSectionCoord(z))) continue; // don't load chunks placing things around

				boolean foundWindSource = false;
				int windSourceY = 0;
				int windSourceHeight = 0;

				for (int y = minY; true; y++) {
					mpos.set(x, y, z);
					BlockState state = level.getBlockState(mpos);
					FluidState fluidState = level.getFluidState(mpos);
					int blockStateWindSourceHeight = WindSourceRegistry.get().getWindSourceHeight(state);

					if (foundWindSource) {
						int height = y - windSourceY;
						if (height > windSourceHeight || // go 1 block beyond to provide margin for top part
								blockStateWindSourceHeight > 0 ||
								!fluidState.isEmpty() ||
								!ParagliderUtils.windCanPassThrough(level, mpos, state)) {
							if (height > 1) wind.write(x, windSourceY, z, height, level.getGameTime());
							foundWindSource = false;
						} else continue;
					}

					if (y > maxY) break;
					if (blockStateWindSourceHeight > 0) {
						foundWindSource = true;
						windSourceY = y;
						windSourceHeight = blockStateWindSourceHeight;
					}
				}
			}
		}
	}

	/**
	 * Checks if placed wind is still valid - that is still having wind source at root position, and isn't expired yet.
	 * All invalid winds will be removed.
	 */
	private static void checkPlacedWind(WindLevel wind, Level level) {
		for (WindChunk windChunk : wind.chunks()) {
			var it = windChunk.nodes.byte2ObjectEntrySet().iterator();
			while (it.hasNext()) {
				var e = it.next();
				byte xz = e.getByteKey();
				WindNode node = e.getValue();

				WindNode updated = validate(wind, windChunk, xz, node, level);
				if (updated != node) {
					if (updated == null) it.remove();
					else windChunk.nodes.put(xz, updated);
				}
			}
		}
	}

	/**
	 * Actually checks things.
	 *
	 * @return Instance of valid wind node; could be {@code null} if there's no valid wind nodes.
	 */
	private static @Nullable WindNode validate(WindLevel wind, WindChunk windChunk, byte xz, WindNode node, Level level) {
		long gameTime = level.getGameTime();

		if (node.updatedTime != gameTime) {
			if (node.isExpired(gameTime) ||
					WindSourceRegistry.get().getWindSourceHeight(level.getBlockState(
							mpos.set(windChunk.x(xz), node.y, windChunk.z(xz)))) <= 0) {
				wind.dirtyWindChunks().add(windChunk.chunkPos.pack());
				return node.next != null ? validate(wind, windChunk, xz, node.next, level) : null;
			}
			node.updatedTime = gameTime;
		}

		if (node.next != null)
			node.next = validate(wind, windChunk, xz, node.next, level);
		return node;
	}

	public static double getWindAbove(Level level, AABB boundingBox) {
		int maxWindY = getMaxWindY(level,
				Mth.floor(boundingBox.minX), Mth.floor(boundingBox.minY), Mth.floor(boundingBox.minZ),
				Mth.ceil(boundingBox.maxX) - 1, Mth.ceil(boundingBox.maxY) - 1, Mth.ceil(boundingBox.maxZ) - 1);
		return Math.max(0, (double)maxWindY - boundingBox.minY);
	}

	private static int getMaxWindY(Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		WindLevel wind = WindLevel.of(level);
		if (wind == null) return 0;

		int chunkXStart = minX >> 4;
		int chunkXEnd = maxX >> 4;
		int chunkZStart = minZ >> 4;
		int chunkZEnd = maxZ >> 4;
		int maxWindY = Integer.MIN_VALUE;

		for (int x = chunkXStart; x <= chunkXEnd; x++) {
			for (int z = chunkZStart; z <= chunkZEnd; z++) {
				WindChunk windChunk = wind.getChunk(x, z);
				if (windChunk != null) {
					maxWindY = Math.max(maxWindY, getMaxWindY(windChunk, minX, minY, minZ, maxX, maxY, maxZ));
				}
			}
		}

		return maxWindY;
	}

	private static int getMaxWindY(WindChunk chunk, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		int xs = Math.max(chunk.chunkPos.getMinBlockX(), minX);
		int xe = Math.min(chunk.chunkPos.getMaxBlockX(), maxX);
		int zs = Math.max(chunk.chunkPos.getMinBlockZ(), minZ);
		int ze = Math.min(chunk.chunkPos.getMaxBlockZ(), maxZ);
		int maxWindY = Integer.MIN_VALUE;

		for (int x = xs; x <= xe; x++) {
			for (int z = zs; z <= ze; z++) {
				WindNode node = chunk.getNode(x, z);
				while (node != null) {
					if (node.y <= maxY && node.y + node.height > minY) {
						maxWindY = Math.max(maxWindY, node.y + node.height);
					}
					node = node.next;
				}
			}
		}

		return maxWindY;
	}
}
