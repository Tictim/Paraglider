package tictim.paraglider.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncRemoteMovementMsg(UUID entityId, Identifier state) implements CustomPacketPayload {
	public static final Type<SyncRemoteMovementMsg> TYPE = new Type<>(id("sync_remote_movement"));
	public static final StreamCodec<ByteBuf, SyncRemoteMovementMsg> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SyncRemoteMovementMsg::entityId,
			Identifier.STREAM_CODEC, SyncRemoteMovementMsg::state,
			SyncRemoteMovementMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
