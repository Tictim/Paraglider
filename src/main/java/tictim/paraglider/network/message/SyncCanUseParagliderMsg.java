package tictim.paraglider.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncCanUseParagliderMsg(
		boolean canUseParaglider,
		boolean canRideUpdraft
) implements CustomPacketPayload {
	public static final Type<SyncCanUseParagliderMsg> TYPE = new Type<>(id("sync_can_use_paraglider"));
	public static final StreamCodec<ByteBuf, SyncCanUseParagliderMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, SyncCanUseParagliderMsg::canUseParaglider,
			ByteBufCodecs.BOOL, SyncCanUseParagliderMsg::canRideUpdraft,
			SyncCanUseParagliderMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
