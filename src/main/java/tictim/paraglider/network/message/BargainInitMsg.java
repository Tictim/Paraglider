package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.network.NetUtils;

import java.util.List;

import static tictim.paraglider.api.ParagliderAPI.id;

public record BargainInitMsg(
		int sessionId,
		@NotNull List<BargainCatalog> catalog,
		@Nullable Vec3 lookAt,
		@Nullable Component dialog
) implements CustomPacketPayload {
	public static final Type<BargainInitMsg> TYPE = new Type<>(id("bargain_init"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainInitMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainInitMsg::sessionId,
			BargainCatalog.STREAM_CODEC.apply(ByteBufCodecs.list()), BargainInitMsg::catalog,
			NetUtils.nullable(Vec3.STREAM_CODEC), BargainInitMsg::lookAt,
			NetUtils.nullable(ComponentSerialization.STREAM_CODEC), BargainInitMsg::dialog,
			BargainInitMsg::new
	);

	@Override public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
