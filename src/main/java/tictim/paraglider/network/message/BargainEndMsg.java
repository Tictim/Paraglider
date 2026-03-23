package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record BargainEndMsg(int sessionId) implements CustomPacketPayload {
	public static final Type<BargainEndMsg> TYPE = new Type<>(id("bargain_end"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainEndMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainEndMsg::sessionId,
			BargainEndMsg::new
	);

	@Override public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
