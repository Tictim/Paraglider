package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.config.Cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class Wind{
	private static final int XZ_RAD_HALF = 4;
	private static final int GROUND_Y_MIN = -2;
	private static final int GROUND_Y_MAX = 4;
	private static final int PARAGLIDING_Y_MIN = -11;
	private static final int PARAGLIDING_Y_MAX = 1;

	private static final Map<LevelAccessor, Wind> windInstances = new Object2ObjectOpenHashMap<>();

	public static void registerLevel(@NotNull LevelAccessor level){
		windInstances.computeIfAbsent(level, l -> new Wind());
	}
	public static void unregisterLevel(@NotNull LevelAccessor level){
		windInstances.remove(level);
	}

	@Nullable public static Wind of(@NotNull LevelAccessor level){
		return windInstances.get(level);
	}

	private Wind(){}

	private final Long2ObjectMap<WindChunk> windChunks = new Long2ObjectOpenHashMap<>();
	private final LongSet dirtyWindChunks = new LongOpenHashSet();

	private final BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

	@Nullable private WindChunk windChunkCache;

	@NotNull @Unmodifiable public Collection<@NotNull WindChunk> windChunks(){
		return Collections.unmodifiableCollection(windChunks.values());
	}
	@NotNull public LongSet dirtyWindChunks(){
		return dirtyWindChunks;
	}

	@Nullable public WindChunk getChunk(@NotNull ChunkPos chunkPos){
		return getChunk(chunkPos.x, chunkPos.z);
	}
	@Nullable public WindChunk getChunk(int chunkX, int chunkZ){
		return getChunk(ChunkPos.asLong(chunkX, chunkZ));
	}
	@Nullable public WindChunk getChunk(long chunkPos){
		return windChunks.get(chunkPos);
	}

	@NotNull public WindChunk getOrCreate(@NotNull ChunkPos chunkPos){
		return getOrCreate(chunkPos.toLong());
	}
	@NotNull public WindChunk getOrCreate(int chunkX, int chunkZ){
		return getOrCreate(ChunkPos.asLong(chunkX, chunkZ));
	}
	@NotNull public WindChunk getOrCreate(long chunkPos){
		return windChunks.computeIfAbsent(chunkPos, cp -> new WindChunk(new ChunkPos(cp)));
	}

	@Nullable public WindChunk remove(int chunkX, int chunkZ){
		return remove(ChunkPos.asLong(chunkX, chunkZ));
	}
	@Nullable public WindChunk remove(@NotNull ChunkPos chunkPos){
		return remove(chunkPos.toLong());
	}
	@Nullable public WindChunk remove(long chunkPos){
		WindChunk removed = windChunks.remove(chunkPos);
		if(removed!=null) removed.setRemoved();
		return removed;
	}

	public void put(@NotNull WindChunk windChunk){
		if(windChunk.isRemoved()) throw new IllegalArgumentException("Cannot add back a removed wind chunk!");
		windChunks.put(windChunk.chunkPos.toLong(), windChunk);
	}

	public void writeWind(int x, int y, int z, int height, long gameTime){
		long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
		if(this.windChunkCache==null||windChunkCache.isRemoved()||windChunkCache.chunkPos.toLong()!=chunkPos){
			this.windChunkCache = getOrCreate(chunkPos);
		}
		if(this.windChunkCache.add(x, y, z, height, gameTime)){
			dirtyWindChunks().add(chunkPos);
		}
	}

	/**
	 * Scans blocks around player and update wind chunks. Scan range is predefined.
	 */
	public void placeAround(@NotNull Player player){
		int x = Mth.floor(player.getX());
		int y = Mth.floor(player.getY());
		int z = Mth.floor(player.getZ());

		place(player.level(),
				x-XZ_RAD_HALF, y+(player.onGround() ? GROUND_Y_MIN : PARAGLIDING_Y_MIN), z-XZ_RAD_HALF,
				x+XZ_RAD_HALF, y+(player.onGround() ? GROUND_Y_MAX : PARAGLIDING_Y_MAX), z+XZ_RAD_HALF);
	}

	/**
	 * Scans blocks in range and update wind chunks.
	 */
	@SuppressWarnings("deprecation")
	private void place(@NotNull Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		for(int x = minX; x<=maxX; x++){
			for(int z = minZ; z<=maxZ; z++){
				if(!level.getChunkSource().hasChunk(
						SectionPos.blockToSectionCoord(x),
						SectionPos.blockToSectionCoord(z))) continue; // don't load chunks placing things around

				boolean hasFireY = false;
				int fireY = 0;
				for(int y = minY; true; y++){
					mpos.set(x, y, z);
					BlockState state = level.getBlockState(mpos);
					boolean isWindSource = Cfg.get().isWindSource(state);

					if(hasFireY){
						int height = y-fireY;
						if(height>=10||
								isWindSource||
								state.blocksMotion()||
								Block.canSupportCenter(level, mpos, Direction.DOWN)||
								Block.canSupportCenter(level, mpos, Direction.UP)){
							if(height>2) writeWind(x, fireY, z, height, level.getGameTime());
							hasFireY = false;
						}else continue;
					}
					if(y>maxY) break;
					if(isWindSource){
						fireY = y;
						hasFireY = true;
					}
				}
			}
		}
	}

	/**
	 * Checks if placed wind is still valid - that is still having wind source at root position, and isn't expired yet.
	 * All invalid winds will be removed.
	 */
	public void checkPlacedWind(@NotNull Level level){
		for(WindChunk windChunk : windChunks()){
			Collection<WindNode> allRootNodes = windChunk.getAllRootNodes();
			for(WindNode node : allRootNodes.toArray(new WindNode[0])){
				WindNode updated = validate(windChunk, node, level);
				if(updated!=node){
					if(updated==null) windChunk.removeAllNodesInXZ(node.x, node.z);
					else windChunk.putNode(updated);
				}
			}
		}
	}

	/**
	 * Actually checks things.
	 *
	 * @return Instance of valid wind node; could be {@code null} if there's no valid wind nodes.
	 */
	@Nullable private WindNode validate(@NotNull WindChunk windChunk, @NotNull WindNode node, @NotNull Level level){
		long gameTime = level.getGameTime();

		if(node.updatedTime!=gameTime){
			if(node.isExpired(gameTime)||
					!Cfg.get().isWindSource(level.getBlockState(mpos.set(node.x, node.y, node.z)))){
				dirtyWindChunks().add(windChunk.chunkPos.toLong());
				return node.next!=null ? validate(windChunk, node.next, level) : null;
			}
			node.updatedTime = gameTime;
		}

		if(node.next!=null)
			node.next = validate(windChunk, node.next, level);
		return node;
	}

	public static boolean isInside(@NotNull Level level, @NotNull AABB boundingBox){
		return isInside(level,
				Mth.floor(boundingBox.minX),
				Mth.floor(boundingBox.minY),
				Mth.floor(boundingBox.minZ),
				Mth.ceil(boundingBox.maxX),
				Mth.ceil(boundingBox.maxY),
				Mth.ceil(boundingBox.maxZ));
	}

	public static boolean isInside(@NotNull Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		Wind wind = of(level);
		if(wind==null) return false;

		int chunkXStart = minX>>4;
		int chunkXEnd = maxX>>4;
		int chunkZStart = minZ>>4;
		int chunkZEnd = maxZ>>4;

		for(int x = chunkXStart; x<=chunkXEnd; x++){
			for(int z = chunkZStart; z<=chunkZEnd; z++){
				WindChunk windChunk = wind.getChunk(x, z);
				if(windChunk!=null&&windChunk.isInsideWind(minX, minY, minZ, maxX, maxY, maxZ)) return true;
			}
		}
		return false;
	}
}
