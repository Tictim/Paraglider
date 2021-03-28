package tictim.paraglider.capabilities.wind;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Collection;

public class Wind implements ICapabilityProvider{
	@CapabilityInject(Wind.class)
	public static Capability<Wind> CAP = null;

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

	@Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==CAP ? self.cast() : LazyOptional.empty();
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable public static Wind of(ICapabilityProvider capabilityProvider){
		return capabilityProvider.getCapability(CAP).orElse(null);
	}
}
