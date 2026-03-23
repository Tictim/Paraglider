package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.wind.WindChunk;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncWindMsg(WindChunk windChunk) implements CustomPacketPayload {
	public static final Type<SyncWindMsg> TYPE = new Type<>(id("sync_wind"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncWindMsg> CODEC = StreamCodec.composite(
			WindChunk.STREAM_CODEC, SyncWindMsg::windChunk,
			SyncWindMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
