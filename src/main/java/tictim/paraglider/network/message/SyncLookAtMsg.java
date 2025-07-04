package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.network.NetUtils;

import static tictim.paraglider.api.ParagliderAPI.id;

public record SyncLookAtMsg(int sessionId, @Nullable Vec3 lookAt) implements CustomPacketPayload {
	public static final Type<SyncLookAtMsg> TYPE = new Type<>(id("sync_look_at"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncLookAtMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SyncLookAtMsg::sessionId,
			NetUtils.VEC3, SyncLookAtMsg::lookAt,
			SyncLookAtMsg::new
	);

	@Override public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
