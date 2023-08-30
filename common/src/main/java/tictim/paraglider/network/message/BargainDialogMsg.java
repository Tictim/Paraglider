package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record BargainDialogMsg(int sessionId, @NotNull Component dialog) implements Msg{
	@NotNull public static BargainDialogMsg read(@NotNull FriendlyByteBuf buffer){
		return new BargainDialogMsg(buffer.readVarInt(), buffer.readComponent());
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
		buffer.writeComponent(dialog);
	}
}
