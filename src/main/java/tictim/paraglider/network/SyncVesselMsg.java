package tictim.paraglider.network;

import com.google.common.base.MoreObjects;
import net.minecraft.network.PacketBuffer;

public final class SyncVesselMsg{
	public final int stamina;
	public final int heartContainers;
	public final int staminaVessels;

	public SyncVesselMsg(int stamina, int heartContainers, int staminaVessels){
		this.stamina = stamina;
		this.heartContainers = heartContainers;
		this.staminaVessels = staminaVessels;
	}

	public void write(PacketBuffer buffer){
		buffer.writeInt(stamina);
		buffer.writeVarInt(heartContainers);
		buffer.writeVarInt(staminaVessels);
	}

	@Override public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("stamina", stamina)
				.add("heartContainers", heartContainers)
				.add("staminaVessels", staminaVessels)
				.toString();
	}

	public static SyncVesselMsg read(PacketBuffer buffer){
		return new SyncVesselMsg(
				buffer.readInt(),
				buffer.readVarInt(),
				buffer.readVarInt()
		);
	}
}
