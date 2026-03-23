package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncMovementMsg(
		Identifier state,
		double stamina,
		double extraStamina,
		boolean depleted,
		int recoveryDelay,
		double efficiency
) implements CustomPacketPayload {
	public static final Type<SyncMovementMsg> TYPE = new Type<>(id("sync_movement"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncMovementMsg> CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, SyncMovementMsg::state,
			ByteBufCodecs.DOUBLE, SyncMovementMsg::stamina,
			ByteBufCodecs.DOUBLE, SyncMovementMsg::extraStamina,
			ByteBufCodecs.BOOL, SyncMovementMsg::depleted,
			ByteBufCodecs.VAR_INT, SyncMovementMsg::recoveryDelay,
			ByteBufCodecs.DOUBLE, SyncMovementMsg::efficiency,
			SyncMovementMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
