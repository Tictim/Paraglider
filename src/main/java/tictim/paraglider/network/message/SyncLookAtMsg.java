package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncLookAtMsg(int sessionId, Optional<Vec3> lookAt) implements CustomPacketPayload {
	public static final Type<SyncLookAtMsg> TYPE = new Type<>(id("sync_look_at"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncLookAtMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SyncLookAtMsg::sessionId,
			ByteBufCodecs.optional(Vec3.STREAM_CODEC), SyncLookAtMsg::lookAt,
			SyncLookAtMsg::new
	);

	@Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
