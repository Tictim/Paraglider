package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.recipe.QuantifiedIngredient;
import tictim.paraglider.contents.recipe.QuantifiedItem;

public record SimplePreview(
		@NotNull SlotDisplay display,
		int quantity
) implements BargainPreview<SimplePreview> {
	public static final Type<SimplePreview> TYPE = new Type<SimplePreview>(StreamCodec.of(
			(buffer, simplePreview) -> {
				SlotDisplay.STREAM_CODEC.encode(buffer, simplePreview.display);
				buffer.writeVarInt(simplePreview.quantity);
			},
			buffer -> new SimplePreview(
					SlotDisplay.STREAM_CODEC.decode(buffer),
					buffer.readVarInt())
	));

	public SimplePreview(QuantifiedIngredient ingredient) {
		this(ingredient.ingredient().display(), ingredient.quantity());
	}

	public SimplePreview(QuantifiedItem item) {
		this(new SlotDisplay.ItemStackSlotDisplay(item.item()), item.quantity());
	}

	@Override public @NotNull Type<SimplePreview> type() {
		return TYPE;
	}
}
