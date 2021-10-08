package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public record SyncLookAtMsg(@Nullable Vec3 lookAt){
	public static SyncLookAtMsg read(FriendlyByteBuf buffer){
		return buffer.readBoolean() ?
				new SyncLookAtMsg(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())) :
				new SyncLookAtMsg(null);
	}

	public SyncLookAtMsg(@Nullable Vec3 lookAt){
		this.lookAt = lookAt;
	}

	public void write(FriendlyByteBuf buffer){
		buffer.writeBoolean(lookAt!=null);
		if(lookAt!=null){
			buffer.writeDouble(lookAt.x);
			buffer.writeDouble(lookAt.y);
			buffer.writeDouble(lookAt.z);
		}
	}
}
