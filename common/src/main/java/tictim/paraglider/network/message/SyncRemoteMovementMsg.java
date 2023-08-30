package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SyncRemoteMovementMsg(@NotNull UUID entityId, @NotNull ResourceLocation state) implements Msg{
	@NotNull public static SyncRemoteMovementMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncRemoteMovementMsg(buffer.readUUID(), buffer.readResourceLocation());
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeUUID(entityId);
		buffer.writeResourceLocation(state);
	}
}
