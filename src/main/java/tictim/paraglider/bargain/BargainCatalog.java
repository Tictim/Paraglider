package tictim.paraglider.bargain;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.network.NetUtils;

import java.util.List;

public record BargainCatalog(
		@NotNull Identifier bargain,
		@NotNull List<@NotNull BargainPreview<?>> demands,
		@NotNull List<@NotNull BargainPreview<?>> offers,
		@NotNull IntList demandCounts,
		boolean canBargain
) {
	public static final StreamCodec<RegistryFriendlyByteBuf, BargainCatalog> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, BargainCatalog::bargain,
			NetUtils.BARGAIN_PREVIEW.apply(ByteBufCodecs.list()), BargainCatalog::demands,
			NetUtils.BARGAIN_PREVIEW.apply(ByteBufCodecs.list()), BargainCatalog::offers,
			NetUtils.INT_LIST, BargainCatalog::demandCounts,
			ByteBufCodecs.BOOL, BargainCatalog::canBargain,
			BargainCatalog::new
	);

	public int getCount(int index) {
		return index < 0 || index >= this.demandCounts.size() ? 0 : this.demandCounts.getInt(index);
	}
}
