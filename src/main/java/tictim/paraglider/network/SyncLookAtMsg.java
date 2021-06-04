package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class SyncLookAtMsg{
	public static SyncLookAtMsg read(PacketBuffer buffer){
		return buffer.readBoolean() ?
				new SyncLookAtMsg(new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())) :
				new SyncLookAtMsg(null);
	}

	@Nullable public final Vector3d lookAt;

	public SyncLookAtMsg(@Nullable Vector3d lookAt){
		this.lookAt = lookAt;
	}

	public void write(PacketBuffer buffer){
		buffer.writeBoolean(lookAt!=null);
		if(lookAt!=null){
			buffer.writeDouble(lookAt.x);
			buffer.writeDouble(lookAt.y);
			buffer.writeDouble(lookAt.z);
		}
	}
}
