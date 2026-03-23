package tictim.paraglider.network.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.bargain.BargainCatalog;

import java.util.List;

import static tictim.paraglider.api.ParagliderAPI.id;

@NullMarked
public record SyncCatalogMsg(
		int sessionId,
		List<BargainCatalog> catalog
) implements CustomPacketPayload {
	public static final Type<SyncCatalogMsg> TYPE = new Type<>(id("sync_catalog"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncCatalogMsg> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SyncCatalogMsg::sessionId,
			BargainCatalog.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncCatalogMsg::catalog,
			SyncCatalogMsg::new
	);

	@Override public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
