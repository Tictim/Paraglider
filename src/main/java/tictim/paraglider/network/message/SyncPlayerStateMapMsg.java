package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.impl.movement.PlayerStateMap;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncPlayerStateMapMsg(PlayerStateMap stateMap) implements CustomPacketPayload {
	public static final Type<SyncPlayerStateMapMsg> TYPE = new Type<>(id("sync_player_state_map"));
	public static final StreamCodec<FriendlyByteBuf, SyncPlayerStateMapMsg> CODEC = StreamCodec.composite(
			PlayerStateMap.STREAM_CODEC, SyncPlayerStateMapMsg::stateMap,
			SyncPlayerStateMapMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
