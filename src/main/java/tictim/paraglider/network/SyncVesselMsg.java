package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;

public record SyncVesselMsg(int stamina, int heartContainers, int staminaVessels){
	public static SyncVesselMsg read(FriendlyByteBuf buffer){
		return new SyncVesselMsg(
				buffer.readInt(),
				buffer.readVarInt(),
				buffer.readVarInt()
		);
	}

	public void write(FriendlyByteBuf buffer){
		buffer.writeInt(stamina);
		buffer.writeVarInt(heartContainers);
		buffer.writeVarInt(staminaVessels);
	}
}
