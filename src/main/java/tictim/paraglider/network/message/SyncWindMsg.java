package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.wind.WindChunk;

import static tictim.paraglider.api.ParagliderAPI.id;

public record SyncWindMsg(@NotNull WindChunk windChunk) implements CustomPacketPayload {
	public static final Type<SyncWindMsg> TYPE = new Type<>(id("sync_wind"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncWindMsg> CODEC = StreamCodec.composite(
			WindChunk.STREAM_CODEC, SyncWindMsg::windChunk,
			SyncWindMsg::new
	);

	@Override @NotNull public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
