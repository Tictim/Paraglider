package tictim.paraglider.contents.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Pair of an ingredient and a non-negative int.
 */
public record QuantifiedIngredient(
		@NotNull Ingredient ingredient,
		@Range(from = 0, to = Integer.MAX_VALUE) int quantity
) implements Predicate<ItemStack> {
	public static final MapCodec<QuantifiedIngredient> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(QuantifiedIngredient::ingredient),
			Codec.INT.fieldOf("quantity").forGetter(QuantifiedIngredient::quantity)
	).apply(b, QuantifiedIngredient::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, QuantifiedIngredient> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, QuantifiedIngredient::ingredient,
			ByteBufCodecs.VAR_INT, QuantifiedIngredient::quantity,
			QuantifiedIngredient::new
	);

	public QuantifiedIngredient(@NotNull Ingredient ingredient, int quantity) {
		this.ingredient = Objects.requireNonNull(ingredient);
		this.quantity = Math.max(0, quantity);
	}

	/**
	 * Test the ItemStack using ingredient. Does not count quantity.
	 */
	@Override public boolean test(ItemStack itemStack) {
		return this.ingredient.test(itemStack);
	}
}
