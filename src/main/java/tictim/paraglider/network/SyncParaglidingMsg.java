package tictim.paraglider.network;

import com.google.common.base.MoreObjects;
import net.minecraft.network.PacketBuffer;
import tictim.paraglider.capabilities.PlayerMovement;

import java.util.UUID;

public class SyncParaglidingMsg{
	public final UUID playerId;
	public final boolean paragliding;

	public SyncParaglidingMsg(PlayerMovement movement){
		this(movement.player.getUniqueID(), movement.isParagliding());
	}
	public SyncParaglidingMsg(UUID playerId, boolean paragliding){
		this.playerId = playerId;
		this.paragliding = paragliding;
	}

	public void write(PacketBuffer buffer){
		buffer.writeUniqueId(playerId);
		buffer.writeBoolean(paragliding);
	}

	@Override public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("playerId", playerId)
				.add("paragliding", paragliding)
				.toString();
	}

	public static SyncParaglidingMsg read(PacketBuffer buffer){
		return new SyncParaglidingMsg(
				buffer.readUniqueId(),
				buffer.readBoolean()
		);
	}
}
