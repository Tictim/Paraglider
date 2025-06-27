package tictim.paraglider.contents.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for
 * {@code 2^31-1} items.
 */
public record QuantifiedItem(@NotNull ItemStack item, int quantity) {
	public static final MapCodec<QuantifiedItem> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			ItemStack.CODEC.fieldOf("item").forGetter(QuantifiedItem::item),
			Codec.INT.optionalFieldOf("quantity", 1).forGetter(QuantifiedItem::quantity)
	).apply(b, QuantifiedItem::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, QuantifiedItem> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, QuantifiedItem::item,
			ByteBufCodecs.VAR_INT, QuantifiedItem::quantity,
			QuantifiedItem::new
	);

	public QuantifiedItem(@NotNull Item item, int quantity) {
		this(new ItemStack(item), Math.max(0, quantity));
	}

	public QuantifiedItem(@NotNull ItemStack item, int quantity) {
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	@NotNull public ItemStack getItem() {
		return item;
	}
	public int getQuantity() {
		return quantity;
	}

	@NotNull public ItemStack getItemWithQuantity() {
		ItemStack copy = item.copy();
		copy.setCount(quantity);
		return copy;
	}
}
