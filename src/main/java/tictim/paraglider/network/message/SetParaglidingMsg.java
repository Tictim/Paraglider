package tictim.paraglider.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static tictim.paraglider.api.ParagliderAPI.id;

public record SetParaglidingMsg(
		boolean paragliding
) implements CustomPacketPayload {
	public static final Type<SetParaglidingMsg> TYPE = new Type<>(id("set_paragliding"));
	public static final StreamCodec<ByteBuf, SetParaglidingMsg> CODEC =
			ByteBufCodecs.BOOL.map(SetParaglidingMsg::new, SetParaglidingMsg::paragliding);

	@Override public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
