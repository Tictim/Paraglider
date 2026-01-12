package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.bargain.BargainCatalog;

import java.util.List;
import java.util.Optional;

import static tictim.paraglider.api.ParagliderAPI.id;

public record BargainInitMsg(
		int sessionId,
		@NotNull List<BargainCatalog> catalog,
		@NotNull Optional<Vec3> lookAt,
		@NotNull Optional<Component> dialog
) implements CustomPacketPayload {
	public static final Type<BargainInitMsg> TYPE = new Type<>(id("bargain_init"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainInitMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, BargainInitMsg::sessionId,
			BargainCatalog.STREAM_CODEC.apply(ByteBufCodecs.list()), BargainInitMsg::catalog,
			ByteBufCodecs.optional(Vec3.STREAM_CODEC), BargainInitMsg::lookAt,
			ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), BargainInitMsg::dialog,
			BargainInitMsg::new
	);

	@Override public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
