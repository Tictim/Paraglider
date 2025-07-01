package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class Wind {
	private static final int XZ_RAD_HALF = 4;
	private static final int GROUND_Y_MIN = -2;
	private static final int GROUND_Y_MAX = 4;
	private static final int PARAGLIDING_Y_MAX = 1;

	private static final Map<LevelAccessor, Wind> windInstances = new Object2ObjectOpenHashMap<>();

	public static void registerLevel(@NotNull LevelAccessor level) {
		windInstances.computeIfAbsent(level, l -> new Wind());
	}
	public static void unregisterLevel(@NotNull LevelAccessor level) {
		windInstances.remove(level);
	}

	public static @Nullable Wind of(@NotNull LevelAccessor level) {
		return windInstances.get(level);
	}

	private final Long2ObjectMap<WindChunk> windChunks = new Long2ObjectOpenHashMap<>();
	private final LongSet dirtyWindChunks = new LongOpenHashSet();

	private final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

	private @Nullable WindChunk windChunkCache;

	private Wind() {}

	public @NotNull @Unmodifiable Collection<@NotNull WindChunk> windChunks() {
		return Collections.unmodifiableCollection(this.windChunks.values());
	}
	public @NotNull LongSet dirtyWindChunks() {
		return this.dirtyWindChunks;
	}

	public @Nullable WindChunk getChunk(@NotNull ChunkPos chunkPos) {
		return getChunk(chunkPos.x, chunkPos.z);
	}
	public @Nullable WindChunk getChunk(int chunkX, int chunkZ) {
		return getChunk(ChunkPos.asLong(chunkX, chunkZ));
	}
	public @Nullable WindChunk getChunk(long chunkPos) {
		return this.windChunks.get(chunkPos);
	}

	public @NotNull WindChunk getOrCreate(@NotNull ChunkPos chunkPos) {
		return getOrCreate(chunkPos.toLong());
	}
	public @NotNull WindChunk getOrCreate(int chunkX, int chunkZ) {
		return getOrCreate(ChunkPos.asLong(chunkX, chunkZ));
	}
	public @NotNull WindChunk getOrCreate(long chunkPos) {
		return this.windChunks.computeIfAbsent(chunkPos, cp -> new WindChunk(new ChunkPos(cp)));
	}

	public @Nullable WindChunk remove(int chunkX, int chunkZ) {
		return remove(ChunkPos.asLong(chunkX, chunkZ));
	}
	public @Nullable WindChunk remove(@NotNull ChunkPos chunkPos) {
		return remove(chunkPos.toLong());
	}
	public @Nullable WindChunk remove(long chunkPos) {
		WindChunk removed = this.windChunks.remove(chunkPos);
		if (removed != null) removed.setRemoved();
		return removed;
	}

	public void put(@NotNull WindChunk windChunk) {
		if (windChunk.isRemoved()) throw new IllegalArgumentException("Cannot add back a removed wind chunk!");
		this.windChunks.put(windChunk.chunkPos.toLong(), windChunk);
	}

	public void writeWind(int x, int y, int z, int height, long gameTime) {
		long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
		if (this.windChunkCache == null || this.windChunkCache.isRemoved() || this.windChunkCache.chunkPos.toLong() != chunkPos) {
			this.windChunkCache = getOrCreate(chunkPos);
		}
		if (this.windChunkCache.add(x, y, z, height, gameTime)) {
			dirtyWindChunks().add(chunkPos);
		}
	}

	/**
	 * Scans blocks around player and update wind chunks. Scan range is predefined.
	 */
	public void placeAround(@NotNull Player player) {
		int x = Mth.floor(player.getX());
		int y = Mth.floor(player.getY());
		int z = Mth.floor(player.getZ());

		place(player.level(),
				x - XZ_RAD_HALF, y + (player.onGround() ? GROUND_Y_MIN : -WindSourceRegistry.get().maxWindHeight() - 1), z - XZ_RAD_HALF,
				x + XZ_RAD_HALF, y + (player.onGround() ? GROUND_Y_MAX : PARAGLIDING_Y_MAX), z + XZ_RAD_HALF);
	}

	/**
	 * Scans blocks in range and update wind chunks.
	 */
	@SuppressWarnings("deprecation")
	private void place(@NotNull Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!level.getChunkSource().hasChunk(
						SectionPos.blockToSectionCoord(x),
						SectionPos.blockToSectionCoord(z))) continue; // don't load chunks placing things around

				boolean foundWindSource = false;
				int windSourceY = 0;
				int windSourceHeight = 0;

				for (int y = minY; true; y++) {
					this.mpos.set(x, y, z);
					BlockState state = level.getBlockState(this.mpos);
					int blockStateWindSourceHeight = WindSourceRegistry.get().getWindSourceHeight(state);

					if (foundWindSource) {
						int height = y - windSourceY;
						if (height > windSourceHeight || // go 1 block beyond to provide margin for top part
								blockStateWindSourceHeight > 0 ||
								!ParagliderUtils.windCanPassThrough(level, this.mpos, state)) {
							if (height > 1) writeWind(x, windSourceY, z, height, level.getGameTime());
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
	public void checkPlacedWind(@NotNull Level level) {
		for (WindChunk windChunk : windChunks()) {
			var it = windChunk.nodes.byte2ObjectEntrySet().iterator();
			while (it.hasNext()) {
				var e = it.next();
				byte xz = e.getByteKey();
				WindNode node = e.getValue();

				WindNode updated = validate(windChunk, xz, node, level);
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
	private @Nullable WindNode validate(@NotNull WindChunk windChunk, byte xz, @NotNull WindNode node, @NotNull Level level) {
		long gameTime = level.getGameTime();

		if (node.updatedTime != gameTime) {
			if (node.isExpired(gameTime) ||
					WindSourceRegistry.get().getWindSourceHeight(level.getBlockState(
							mpos.set(windChunk.x(xz), node.y, windChunk.z(xz)))) <= 0) {
				dirtyWindChunks().add(windChunk.chunkPos.toLong());
				return node.next != null ? validate(windChunk, xz, node.next, level) : null;
			}
			node.updatedTime = gameTime;
		}

		if (node.next != null)
			node.next = validate(windChunk, xz, node.next, level);
		return node;
	}

	public static double getWindAbove(@NotNull Level level, @NotNull AABB boundingBox) {
		int maxWindY = getMaxWindY(level,
				Mth.floor(boundingBox.minX), Mth.floor(boundingBox.minY), Mth.floor(boundingBox.minZ),
				Mth.ceil(boundingBox.maxX) - 1, Mth.ceil(boundingBox.maxY) - 1, Mth.ceil(boundingBox.maxZ) - 1);
		return Math.max(0, (double)maxWindY - boundingBox.minY);
	}

	private static int getMaxWindY(@NotNull Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		Wind wind = of(level);
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
