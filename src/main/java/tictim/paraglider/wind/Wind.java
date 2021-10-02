package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import tictim.paraglider.capabilities.Caps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class Wind implements ICapabilityProvider{
	private final Long2ObjectMap<WindChunk> windChunks = new Long2ObjectOpenHashMap<>();

	public void put(WindChunk windChunk){
		this.windChunks.put(windChunk.getChunkPos().asLong(), windChunk);
	}

	@Nullable public WindChunk get(int chunkX, int chunkZ){
		return windChunks.get(ChunkPos.asLong(chunkX, chunkZ));
	}
	@Nullable public WindChunk get(ChunkPos chunkPos){
		return windChunks.get(chunkPos.asLong());
	}

	public WindChunk getOrCreate(int chunkX, int chunkZ){
		WindChunk windChunk = get(chunkX, chunkZ);
		if(windChunk!=null) return windChunk;
		windChunk = new WindChunk(new ChunkPos(chunkX, chunkZ));
		put(windChunk);
		return windChunk;
	}
	public WindChunk getOrCreate(ChunkPos chunkPos){
		WindChunk windChunk = get(chunkPos);
		if(windChunk!=null) return windChunk;
		windChunk = new WindChunk(chunkPos);
		put(windChunk);
		return windChunk;
	}

	@Nullable public WindChunk remove(int chunkX, int chunkZ){
		return windChunks.remove(ChunkPos.asLong(chunkX, chunkZ));
	}
	@Nullable public WindChunk remove(ChunkPos chunkPos){
		return windChunks.remove(chunkPos.asLong());
	}

	public Collection<WindChunk> getWindChunks(){
		return windChunks.values();
	}

	private final LazyOptional<Wind> self = LazyOptional.of(() -> this);

	@Nonnull @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==Caps.wind ? self.cast() : LazyOptional.empty();
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable public static Wind of(ICapabilityProvider capabilityProvider){
		return capabilityProvider.getCapability(Caps.wind).orElse(null);
	}

	public static boolean isInside(World world, AxisAlignedBB boundingBox){
		return isInside(world,
				MathHelper.floor(boundingBox.minX),
				MathHelper.floor(boundingBox.minY),
				MathHelper.floor(boundingBox.minZ),
				MathHelper.ceil(boundingBox.maxX),
				MathHelper.ceil(boundingBox.maxY),
				MathHelper.ceil(boundingBox.maxZ));
	}
	public static boolean isInside(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		Wind wind = of(world);
		if(wind==null) return false;

		int chunkXStart = minX >> 4;
		int chunkXEnd = maxX >> 4;
		int chunkZStart = minZ >> 4;
		int chunkZEnd = maxZ >> 4;

		for(int x = chunkXStart; x<=chunkXEnd; x++){
			for(int z = chunkZStart; z<=chunkZEnd; z++){
				WindChunk windChunk = wind.get(x, z);
				if(windChunk!=null&&windChunk.isInsideWind(minX, minY, minZ, maxX, maxY, maxZ)) return true;
			}
		}
		return false;
	}
}
