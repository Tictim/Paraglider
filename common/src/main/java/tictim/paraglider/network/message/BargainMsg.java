package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BargainMsg(int sessionId, @NotNull ResourceLocation bargain) implements Msg{
	@NotNull public static BargainMsg read(@NotNull FriendlyByteBuf buffer){
		return new BargainMsg(buffer.readVarInt(), buffer.readResourceLocation());
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
		buffer.writeResourceLocation(bargain);
	}
}
