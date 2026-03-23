package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class WindLevel {
	private static final Map<LevelAccessor, WindLevel> windInstances = new Object2ObjectOpenHashMap<>();

	public static void registerLevel(@NotNull LevelAccessor level) {
		windInstances.computeIfAbsent(level, l -> new WindLevel());
	}
	public static void unregisterLevel(@NotNull LevelAccessor level) {
		windInstances.remove(level);
	}

	public static @Nullable WindLevel of(@NotNull LevelAccessor level) {
		return windInstances.get(level);
	}

	private final Long2ObjectMap<WindChunk> chunks = new Long2ObjectOpenHashMap<>();
	private final LongSet dirtyWindChunks = new LongOpenHashSet();

	private @Nullable WindChunk windChunkCache;

	private WindLevel() {}

	public @NotNull @Unmodifiable Collection<@NotNull WindChunk> chunks() {
		return Collections.unmodifiableCollection(this.chunks.values());
	}
	public @NotNull LongSet dirtyWindChunks() {
		return this.dirtyWindChunks;
	}

	public @Nullable WindChunk getChunk(@NotNull ChunkPos chunkPos) {
		return getChunk(chunkPos.x(), chunkPos.z());
	}
	public @Nullable WindChunk getChunk(int chunkX, int chunkZ) {
		return getChunk(ChunkPos.pack(chunkX, chunkZ));
	}
	public @Nullable WindChunk getChunk(long chunkPos) {
		return this.chunks.get(chunkPos);
	}

	public @NotNull WindChunk getOrCreate(@NotNull ChunkPos chunkPos) {
		return getOrCreate(chunkPos.pack());
	}
	public @NotNull WindChunk getOrCreate(int chunkX, int chunkZ) {
		return getOrCreate(ChunkPos.pack(chunkX, chunkZ));
	}
	public @NotNull WindChunk getOrCreate(long chunkPos) {
		return this.chunks.computeIfAbsent(chunkPos, cp -> new WindChunk(ChunkPos.unpack(cp)));
	}

	public @Nullable WindChunk remove(int chunkX, int chunkZ) {
		return remove(ChunkPos.pack(chunkX, chunkZ));
	}
	public @Nullable WindChunk remove(@NotNull ChunkPos chunkPos) {
		return remove(chunkPos.pack());
	}
	public @Nullable WindChunk remove(long chunkPos) {
		WindChunk removed = this.chunks.remove(chunkPos);
		if (removed != null) removed.setRemoved();
		return removed;
	}

	public void putChunk(@NotNull WindChunk windChunk) {
		if (windChunk.isRemoved()) throw new IllegalArgumentException("Cannot add back a removed wind chunk!");
		this.chunks.put(windChunk.chunkPos.pack(), windChunk);
	}

	public void write(int x, int y, int z, int height, long gameTime) {
		long chunkPos = ChunkPos.pack(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
		if (this.windChunkCache == null || this.windChunkCache.isRemoved() || this.windChunkCache.chunkPos.pack() != chunkPos) {
			this.windChunkCache = getOrCreate(chunkPos);
		}
		if (this.windChunkCache.add(x, y, z, height, gameTime)) {
			dirtyWindChunks().add(chunkPos);
		}
	}
}
