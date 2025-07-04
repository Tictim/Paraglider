package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.recipe.QuantifiedItem;

import java.util.List;

public record ItemPreview(
		@NotNull List<ItemStack> display,
		int quantity
) implements BargainPreview<ItemPreview> {
	public static final Type<ItemPreview> TYPE = new Type<>(StreamCodec.composite(
			ItemStack.LIST_STREAM_CODEC, ItemPreview::display,
			ByteBufCodecs.VAR_INT, ItemPreview::quantity,
			ItemPreview::new
	));

	public ItemPreview(QuantifiedItem item) {
		this(List.of(item.item()), item.quantity());
	}

	@Override public @NotNull Type<ItemPreview> type() {
		return TYPE;
	}
}
