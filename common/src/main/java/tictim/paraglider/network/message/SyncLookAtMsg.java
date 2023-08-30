package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.network.NetUtils;

public record SyncLookAtMsg(int sessionId, @Nullable Vec3 lookAt) implements Msg{
	@NotNull public static SyncLookAtMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncLookAtMsg(buffer.readVarInt(), NetUtils.readLookAt(buffer));
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
		NetUtils.writeLookAt(buffer, lookAt);
	}
}
