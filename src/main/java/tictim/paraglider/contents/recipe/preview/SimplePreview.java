package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainPreview;

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

	public SimplePreview(SizedIngredient ingredient) {
		this(ingredient.ingredient().display(), ingredient.count());
	}

	public SimplePreview(ItemStackTemplate item) {
		this(new SlotDisplay.ItemStackSlotDisplay(item), item.count());
	}

	@Override public @NotNull Type<SimplePreview> type() {
		return TYPE;
	}
}
