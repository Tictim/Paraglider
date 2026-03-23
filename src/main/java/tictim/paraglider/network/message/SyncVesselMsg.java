package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncVesselMsg(
		double stamina,
		double extraStamina,
		boolean depleted,
		int heartContainers,
		int staminaVessels
) implements CustomPacketPayload {
	public static final Type<SyncVesselMsg> TYPE = new Type<>(id("sync_vessel"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncVesselMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, SyncVesselMsg::stamina,
			ByteBufCodecs.DOUBLE, SyncVesselMsg::extraStamina,
			ByteBufCodecs.BOOL, SyncVesselMsg::depleted,
			ByteBufCodecs.VAR_INT, SyncVesselMsg::heartContainers,
			ByteBufCodecs.VAR_INT, SyncVesselMsg::staminaVessels,
			SyncVesselMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
