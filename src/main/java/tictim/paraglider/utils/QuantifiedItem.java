package tictim.paraglider.utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for {@code 2^31-1} items.
 */
public record QuantifiedItem(Item item, int quantity){
	public static QuantifiedItem read(FriendlyByteBuf buffer){
		return new QuantifiedItem(Item.byId(buffer.readVarInt()), buffer.readVarInt());
	}

	public QuantifiedItem(Item item, int quantity){
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	public void write(FriendlyByteBuf buffer){
		buffer.writeVarInt(Item.getId(item));
		buffer.writeVarInt(quantity);
	}
}
