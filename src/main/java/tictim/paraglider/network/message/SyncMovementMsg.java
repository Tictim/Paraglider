package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static tictim.paraglider.api.ParagliderAPI.id;

public record SyncMovementMsg(
		@NotNull ResourceLocation state,
		double stamina,
		boolean depleted,
		int recoveryDelay,
		double reductionRate
) implements CustomPacketPayload {
	public static final Type<SyncMovementMsg> TYPE = new Type<>(id("sync_movement"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncMovementMsg> CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, SyncMovementMsg::state,
			ByteBufCodecs.DOUBLE, SyncMovementMsg::stamina,
			ByteBufCodecs.BOOL, SyncMovementMsg::depleted,
			ByteBufCodecs.VAR_INT, SyncMovementMsg::recoveryDelay,
			ByteBufCodecs.DOUBLE, SyncMovementMsg::reductionRate,
			SyncMovementMsg::new
	);

	@Override @NotNull public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
