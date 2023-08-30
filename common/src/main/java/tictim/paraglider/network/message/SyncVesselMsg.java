package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncVesselMsg(int stamina, int heartContainers, int staminaVessels) implements Msg{
	@NotNull public static SyncVesselMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncVesselMsg(
				buffer.readInt(),
				buffer.readVarInt(),
				buffer.readVarInt()
		);
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeInt(stamina);
		buffer.writeVarInt(heartContainers);
		buffer.writeVarInt(staminaVessels);
	}
}
