package tictim.paraglider.wind;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WindNode{
	private static final long WIND_LIFESPAN = 200;

	@Nullable public WindNode next;

	public int x;
	public int y;
	public int z;
	public int height;

	public long createdTime;
	public long updatedTime;

	private WindNode(){}
	public WindNode(int x, int y, int z, int height, long createdTime){
		this(x, y, z, height, createdTime, createdTime);
	}
	public WindNode(int x, int y, int z, int height, long createdTime, long updatedTime){
		this.x = x;
		this.y = y;
		this.z = z;
		this.height = height;
		this.createdTime = createdTime;
		this.updatedTime = updatedTime;
	}
	public WindNode(@NotNull FriendlyByteBuf buf){
		short size = buf.readUnsignedByte();
		read(buf);
		WindNode currentNode = this;
		while(--size>0){
			WindNode next = new WindNode();
			next.read(buf);
			currentNode = currentNode.next = next;
		}
	}

	private void read(@NotNull FriendlyByteBuf buf){
		this.x = buf.readInt();
		this.y = buf.readVarInt();
		this.z = buf.readInt();
		this.height = buf.readVarInt();
	}

	public boolean isExpired(long gameTime){
		return gameTime-createdTime>=WIND_LIFESPAN;
	}

	/**
	 * Overwrites y, height, createdTime. Returns either y or height is changed.<br>
	 * createdTime is excluded because it is not shared to clients.
	 *
	 * @return either y or height is changed
	 */
	public boolean overwrite(int y, int height, long createdTime){
		if(this.y<y){
			if(this.next!=null) return this.next.overwrite(y, height, createdTime);
			else{
				this.next = new WindNode(this.x, y, this.z, height, createdTime);
				return true;
			}
		}else if(this.y==y){
			this.updatedTime = this.createdTime = createdTime;
			if(this.height!=height){
				this.height = height;
				return true;
			}else return false;
		}else{ //this.y>y
			WindNode node = new WindNode(this.x, this.y, this.z, this.height, this.createdTime, this.updatedTime);
			node.next = this.next;
			this.next = node;

			this.y = y;
			this.height = height;
			this.updatedTime = this.createdTime = createdTime;
			return true;
		}
	}

	public void write(@NotNull FriendlyByteBuf buf){
		int w = buf.writerIndex();
		buf.writeByte(0);
		int size = 0;
		for(WindNode n = this; n!=null; n = n.next){
			n.writeThis(buf);
			size++;
		}
		buf.setByte(w, Math.min(size, 255));
	}

	private void writeThis(@NotNull FriendlyByteBuf buf){
		buf.writeInt(x);
		buf.writeVarInt(y);
		buf.writeInt(z);
		buf.writeVarInt(height);
	}
}
