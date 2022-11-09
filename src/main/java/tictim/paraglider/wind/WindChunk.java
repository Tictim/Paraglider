package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public final class WindChunk{
	private final ChunkPos chunkPos;
	private final Byte2ObjectMap<WindNode> nodes = new Byte2ObjectLinkedOpenHashMap<>();

	public WindChunk(ChunkPos chunkPos){
		this.chunkPos = Objects.requireNonNull(chunkPos);
	}
	public WindChunk(PacketBuffer buf){
		chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
		for(int i = buf.readVarInt(); i>0; i--){
			putNode(new WindNode(buf));
		}
	}

	public ChunkPos getChunkPos(){
		return chunkPos;
	}

	@Nullable public WindNode getNode(int x, int z){
		return nodes.get(encode(x, z));
	}
	@Nullable public WindNode putNode(WindNode node){
		return nodes.put(encode(node.x, node.z), node);
	}
	@Nullable public WindNode removeAllNodesInXZ(int x, int z){
		return nodes.remove(encode(x, z));
	}

	public Collection<WindNode> getAllRootNodes(){
		return nodes.values();
	}

	public boolean isEmpty(){
		return nodes.isEmpty();
	}

	private byte encode(int x, int z){
		return (byte)((x<<4&0xF0)|(z&0x0F));
	}

	public boolean isInsideWind(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		if(chunkPos.getXStart()>maxX||chunkPos.getXEnd()<minX||chunkPos.getZStart()>maxZ||chunkPos.getZEnd()<minZ) return false;

		int xs = Math.max(chunkPos.getXStart(), minX);
		int xe = Math.min(chunkPos.getXEnd(), maxX);
		int zs = Math.max(chunkPos.getZStart(), minZ);
		int ze = Math.min(chunkPos.getZEnd(), maxZ);
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

	public void write(PacketBuffer buf){
		buf.writeInt(chunkPos.x);
		buf.writeInt(chunkPos.z);
		buf.writeVarInt(nodes.size());
		for(Byte2ObjectMap.Entry<WindNode> e : nodes.byte2ObjectEntrySet()){
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
