package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record BargainEndMsg(int sessionId) implements Msg{
	@NotNull public static BargainEndMsg read(@NotNull FriendlyByteBuf buffer){
		return new BargainEndMsg(buffer.readVarInt());
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
	}
}
