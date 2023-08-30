package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncMovementMsg(
		@NotNull ResourceLocation state,
		int stamina,
		boolean depleted,
		int recoveryDelay
) implements Msg{
	@NotNull public static SyncMovementMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncMovementMsg(
				buffer.readResourceLocation(),
				buffer.readInt(),
				buffer.readBoolean(),
				buffer.readVarInt());
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeResourceLocation(state);
		buffer.writeInt(stamina);
		buffer.writeBoolean(depleted);
		buffer.writeVarInt(recoveryDelay);
	}
}
