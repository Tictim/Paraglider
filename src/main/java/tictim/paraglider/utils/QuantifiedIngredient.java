package tictim.paraglider.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Pair of an ingredient and an unsigned int.
 */
public final class QuantifiedIngredient implements Predicate<ItemStack>{
	public static QuantifiedIngredient read(PacketBuffer buffer){
		return new QuantifiedIngredient(Ingredient.read(buffer), buffer.readVarInt());
	}

	private final Ingredient ingredient;
	private final int quantity;

	public QuantifiedIngredient(Ingredient ingredient, int quantity){
		this.ingredient = Objects.requireNonNull(ingredient);
		this.quantity = Math.max(0, quantity);
	}
	public QuantifiedIngredient(JsonObject obj){
		this(Ingredient.deserialize(obj.get("ingredient")), Math.max(1, JSONUtils.getInt(obj, "quantity", 1)));
	}

	public Ingredient getIngredient(){
		return ingredient;
	}
	public int getQuantity(){
		return quantity;
	}

	/**
	 * Test the ItemStack using ingredient. Does not count quantity.
	 */
	@Override public boolean test(ItemStack itemStack){
		return ingredient.test(itemStack);
	}

	public JsonElement serialize(){
		JsonObject obj = new JsonObject();
		obj.add("ingredient", ingredient.serialize());
		if(quantity!=1) obj.addProperty("quantity", quantity);
		return obj;
	}

	public void write(PacketBuffer buffer){
		ingredient.write(buffer);
		buffer.writeVarInt(quantity);
	}

	@Override public String toString(){
		return "QuantifiedIngredient{"+
				"ingredient="+ingredient.serialize()+
				", quantity="+quantity+
				'}';
	}
}
