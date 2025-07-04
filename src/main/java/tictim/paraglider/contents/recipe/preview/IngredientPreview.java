package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.recipe.QuantifiedIngredient;

import java.util.Arrays;
import java.util.List;

public record IngredientPreview(
		@NotNull Ingredient ingredient,
		int quantity
) implements BargainPreview<IngredientPreview> {
	public static final Type<IngredientPreview> TYPE = new Type<>(StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, IngredientPreview::ingredient,
			ByteBufCodecs.VAR_INT, IngredientPreview::quantity,
			IngredientPreview::new
	));

	public IngredientPreview(QuantifiedIngredient ingredient) {
		this(ingredient.ingredient(), ingredient.quantity());
	}

	@Override public @NotNull List<ItemStack> display() {
		return Arrays.asList(this.ingredient.getItems());
	}

	@Override public @NotNull Type<IngredientPreview> type() {
		return TYPE;
	}
}
