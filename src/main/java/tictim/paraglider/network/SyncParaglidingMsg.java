package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;
import tictim.paraglider.capabilities.PlayerMovement;

import java.util.UUID;

public record SyncParaglidingMsg(UUID playerId, boolean paragliding){
	public static SyncParaglidingMsg read(FriendlyByteBuf buffer){
		return new SyncParaglidingMsg(
				buffer.readUUID(),
				buffer.readBoolean()
		);
	}

	public SyncParaglidingMsg(PlayerMovement movement){
		this(movement.player.getUUID(), movement.isParagliding());
	}

	public void write(FriendlyByteBuf buffer){
		buffer.writeUUID(playerId);
		buffer.writeBoolean(paragliding);
	}
}
