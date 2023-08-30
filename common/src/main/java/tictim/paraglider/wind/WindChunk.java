package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class WindChunk{
	public final ChunkPos chunkPos;

	private final Byte2ObjectMap<WindNode> nodes = new Byte2ObjectOpenHashMap<>();

	private boolean removed;

	public WindChunk(@NotNull ChunkPos chunkPos){
		this.chunkPos = Objects.requireNonNull(chunkPos);
	}
	public WindChunk(@NotNull FriendlyByteBuf buf){
		this.chunkPos = new ChunkPos(buf.readLong());
		for(int i = buf.readVarInt(); i>0; i--){
			putNode(new WindNode(buf));
		}
	}

	@Nullable public WindNode getNode(int x, int z){
		return nodes.get(encode(x, z));
	}

	public void putNode(@NotNull WindNode node){
		nodes.put(encode(node.x, node.z), node);
	}
	public void removeAllNodesInXZ(int x, int z){
		nodes.remove(encode(x, z));
	}

	@NotNull @Unmodifiable public Collection<@NotNull WindNode> getAllRootNodes(){
		return Collections.unmodifiableCollection(nodes.values());
	}

	public boolean isEmpty(){
		return nodes.isEmpty();
	}

	public boolean isRemoved(){
		return removed;
	}
	void setRemoved(){
		this.removed = true;
	}

	private byte encode(int x, int z){
		return (byte)((x<<4&0b1111_0000)|(z&0b1111));
	}

	public boolean isInsideWind(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		int xMin = chunkPos.getMinBlockX();
		int xMax = chunkPos.getMaxBlockX();
		int zMin = chunkPos.getMinBlockZ();
		int zMax = chunkPos.getMaxBlockZ();
		if(xMin>maxX||xMax<minX||zMin>maxZ||zMax<minZ)
			return false;

		int xs = Math.max(xMin, minX);
		int xe = Math.min(xMax, maxX);
		int zs = Math.max(zMin, minZ);
		int ze = Math.min(zMax, maxZ);
		for(int x = xs; x<=xe; x++){
			for(int z = zs; z<=ze; z++){
				WindNode node = getNode(x, z);
				while(node!=null){
					if(node.y<maxY&&node.y+node.height>minY) return true;
					node = node.next;
				}
			}
		}
		return false;
	}

	public boolean add(int x, int y, int z, int height, long gameTime){
		WindNode node = getNode(x, z);
		if(node!=null){
			return node.overwrite(y, height, gameTime);
		}else{
			putNode(new WindNode(x, y, z, height, gameTime));
			return true;
		}
	}

	public void write(@NotNull FriendlyByteBuf buf){
		buf.writeLong(chunkPos.toLong());
		buf.writeVarInt(nodes.size());
		for(var e : nodes.byte2ObjectEntrySet()){
			e.getValue().write(buf);
		}
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		WindChunk windChunk = (WindChunk)o;
		return chunkPos.equals(windChunk.chunkPos);
	}

	@Override public int hashCode(){
		return chunkPos.hashCode();
	}
}
