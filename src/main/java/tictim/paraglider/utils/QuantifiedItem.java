package tictim.paraglider.utils;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for
 * {@code 2^31-1} items.
 */
public final class QuantifiedItem{
	public static QuantifiedItem read(PacketBuffer buffer){
		return new QuantifiedItem(buffer.readItemStack(), buffer.readVarInt());
	}

	private final ItemStack item;
	private final int quantity;

	public QuantifiedItem(Item item, int quantity){
		this.item = new ItemStack(item);
		this.quantity = Math.max(0, quantity);
	}

	public QuantifiedItem(ItemStack item, int quantity){
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	public QuantifiedItem(JsonObject object){
		ItemStack stack = ShapedRecipe.deserializeItem(object);
		stack.setCount(1);
		this.item = stack;
		this.quantity = JSONUtils.getInt(object, "count", 1);
	}

	public ItemStack getItem(){
		return item;
	}
	public int getQuantity(){
		return quantity;
	}

	public ItemStack getItemWithQuantity(){
		ItemStack copy = item.copy();
		copy.setCount(quantity);
		return copy;
	}

	public JsonObject serialize(){
		JsonObject obj = new JsonObject();
		obj.addProperty("item", ForgeRegistries.ITEMS.getKey(item.getItem())+"");
		if(quantity!=1) obj.addProperty("count", quantity);
		return obj;
	}

	public void write(PacketBuffer buffer){
		buffer.writeItemStack(item);
		buffer.writeVarInt(quantity);
	}

	@Override public String toString(){
		return "QuantifiedItem{"+
				"item="+item+
				", quantity="+quantity+
				'}';
	}
}
