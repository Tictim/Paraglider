package tictim.paraglider.wind;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public final class WindChunk {
	public static final StreamCodec<FriendlyByteBuf, WindChunk> STREAM_CODEC =
			StreamCodec.of((buf, windChunk) -> windChunk.write(buf), WindChunk::new);

	public final ChunkPos chunkPos;
	public final Byte2ObjectMap<WindNode> nodes = new Byte2ObjectOpenHashMap<>();

	private boolean removed;

	public WindChunk(ChunkPos chunkPos) {
		this.chunkPos = Objects.requireNonNull(chunkPos);
	}
	public WindChunk(FriendlyByteBuf buf) {
		this.chunkPos = buf.readChunkPos();
		for (int i = buf.readVarInt(); i > 0; i--) {
			byte xz = buf.readByte();
			putNode(xz, new WindNode(buf));
		}
	}

	public @Nullable WindNode getNode(int x, int z) {
		return this.nodes.get(encode(x, z));
	}

	public void putNode(byte xz, WindNode node) {
		this.nodes.put(xz, node);
	}
	public void removeAllNodesInXZ(int x, int z) {
		this.nodes.remove(encode(x, z));
	}

	public boolean isEmpty() {
		return this.nodes.isEmpty();
	}

	public boolean isRemoved() {
		return this.removed;
	}
	void setRemoved() {
		this.removed = true;
	}

	public int x(byte xz) {
		return ((xz >> 4) & 0b1111) + this.chunkPos.getMinBlockX();
	}
	public int z(byte xz) {
		return (xz & 0b1111) + this.chunkPos.getMinBlockZ();
	}

	private byte encode(int x, int z) {
		return (byte)((x << 4 & 0b1111_0000) | (z & 0b1111));
	}

	public boolean add(int x, int y, int z, int height, long gameTime) {
		byte xz = encode(x, z);
		WindNode node = this.nodes.get(xz);
		if (node != null) {
			return node.overwrite(y, height, gameTime);
		} else {
			putNode(xz, new WindNode(y, height, gameTime));
			return true;
		}
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeChunkPos(this.chunkPos);
		buf.writeVarInt(this.nodes.size());
		for (var e : this.nodes.byte2ObjectEntrySet()) {
			buf.writeByte(e.getByteKey());
			e.getValue().write(buf);
		}
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WindChunk windChunk = (WindChunk)o;
		return this.chunkPos.equals(windChunk.chunkPos);
	}

	@Override public int hashCode() {
		return this.chunkPos.hashCode();
	}
}
