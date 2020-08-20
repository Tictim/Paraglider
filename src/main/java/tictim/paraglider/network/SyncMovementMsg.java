package tictim.paraglider.network;

import com.google.common.base.MoreObjects;
import net.minecraft.network.PacketBuffer;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.PlayerState;

public final class SyncMovementMsg{
	public final PlayerState state;
	public final int stamina;
	public final boolean depleted;
	public final int recoveryDelay;

	public SyncMovementMsg(PlayerMovement h){
		this.state = h.getState();
		this.stamina = h.getStamina();
		this.depleted = h.isDepleted();
		this.recoveryDelay = h.getRecoveryDelay();
	}

	public SyncMovementMsg(PlayerState state, int stamina, boolean depleted, int recoveryDelay){
		this.state = state;
		this.stamina = stamina;
		this.depleted = depleted;
		this.recoveryDelay = recoveryDelay;
	}

	public void copyTo(PlayerMovement h){
		h.setState(state);
		h.setStamina(stamina);
		h.setDepleted(depleted);
		h.setRecoveryDelay(recoveryDelay);
	}

	public void write(PacketBuffer buffer){
		buffer.writeByte(state.ordinal());
		buffer.writeInt(stamina);
		buffer.writeBoolean(depleted);
		buffer.writeVarInt(recoveryDelay);
	}

	@Override public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("state", state)
				.add("stamina", stamina)
				.add("depleted", depleted)
				.add("recoveryDelay", recoveryDelay)
				.toString();
	}

	public static SyncMovementMsg read(PacketBuffer buffer){
		return new SyncMovementMsg(
				PlayerState.of(buffer.readUnsignedByte()),
				buffer.readInt(),
				buffer.readBoolean(),
				buffer.readVarInt());
	}
}
