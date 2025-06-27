package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static tictim.paraglider.api.ParagliderAPI.id;

public record BargainMsg(int sessionId, @NotNull ResourceLocation bargain) implements CustomPacketPayload {
	public static final Type<BargainMsg> TYPE = new Type<>(id("bargain"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainMsg::sessionId,
			ResourceLocation.STREAM_CODEC, BargainMsg::bargain,
			BargainMsg::new
	);

	@Override @NotNull public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
