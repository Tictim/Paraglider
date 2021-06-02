package tictim.paraglider.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for {@code 2^31-1} items.
 */
public final class QuantifiedItem{
	public static QuantifiedItem read(PacketBuffer buffer){
		return new QuantifiedItem(Item.getItemById(buffer.readVarInt()), buffer.readVarInt());
	}

	private final Item item;
	private final int quantity;

	public QuantifiedItem(Item item, int quantity){
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	public Item getItem(){
		return item;
	}
	public int getQuantity(){
		return quantity;
	}

	public void write(PacketBuffer buffer){
		buffer.writeVarInt(Item.getIdFromItem(item));
		buffer.writeVarInt(quantity);
	}

	@Override public String toString(){
		return "QuantifiedItem{"+
				"item="+item+
				", quantity="+quantity+
				'}';
	}
}
