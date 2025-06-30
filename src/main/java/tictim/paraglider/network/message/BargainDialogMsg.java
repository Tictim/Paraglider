package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static tictim.paraglider.api.ParagliderAPI.id;

public record BargainDialogMsg(int sessionId, @NotNull Component dialog) implements CustomPacketPayload {
	public static final Type<BargainDialogMsg> TYPE = new Type<>(id("bargain_dialog"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainDialogMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainDialogMsg::sessionId,
			ComponentSerialization.STREAM_CODEC, BargainDialogMsg::dialog,
			BargainDialogMsg::new
	);

	@Override public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
