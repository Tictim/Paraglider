package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record BargainMsg(int sessionId, Identifier bargain) implements CustomPacketPayload {
	public static final Type<BargainMsg> TYPE = new Type<>(id("bargain"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainMsg::sessionId,
			Identifier.STREAM_CODEC, BargainMsg::bargain,
			BargainMsg::new
	);

	@Override public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
