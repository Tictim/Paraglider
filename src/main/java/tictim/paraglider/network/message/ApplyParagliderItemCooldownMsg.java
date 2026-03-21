package tictim.paraglider.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static tictim.paraglider.api.ParagliderAPI.id;

public class ApplyParagliderItemCooldownMsg implements CustomPacketPayload {
	public static final ApplyParagliderItemCooldownMsg INSTANCE = new ApplyParagliderItemCooldownMsg();

	public static final CustomPacketPayload.Type<ApplyParagliderItemCooldownMsg> TYPE =
			new CustomPacketPayload.Type<>(id("apply_paraglider_item_cooldown"));
	public static final StreamCodec<ByteBuf, ApplyParagliderItemCooldownMsg> CODEC = StreamCodec.unit(INSTANCE);

	@Override public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
