package tictim.paraglider.utils;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for
 * {@code 2^31-1} items.
 */
public record QuantifiedItem(ItemStack item, int quantity){
	public static QuantifiedItem read(FriendlyByteBuf buffer){
		return new QuantifiedItem(buffer.readItem(), buffer.readVarInt());
	}

	public QuantifiedItem(Item item, int quantity){
		this(new ItemStack(item), Math.max(0, quantity));
	}

	public QuantifiedItem(ItemStack item, int quantity){
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	public QuantifiedItem(JsonObject object){
		this(parseItemStack(object), GsonHelper.getAsInt(object, "count", 1));
	}

	private static ItemStack parseItemStack(JsonObject object){
		ItemStack stack = ShapedRecipe.itemStackFromJson(object);
		stack.setCount(1);
		return stack;
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

	public void write(FriendlyByteBuf buffer){
		buffer.writeItem(item);
		buffer.writeVarInt(quantity);
	}
}
