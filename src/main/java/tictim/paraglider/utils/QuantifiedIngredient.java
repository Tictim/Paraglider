package tictim.paraglider.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Pair of an ingredient and an unsigned int.
 */
public record QuantifiedIngredient(Ingredient ingredient, int quantity) implements Predicate<ItemStack>{
	public static QuantifiedIngredient read(FriendlyByteBuf buffer){
		return new QuantifiedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt());
	}

	public QuantifiedIngredient(Ingredient ingredient, int quantity){
		this.ingredient = Objects.requireNonNull(ingredient);
		this.quantity = Math.max(0, quantity);
	}
	public QuantifiedIngredient(JsonObject obj){
		this(Ingredient.fromJson(obj.get("ingredient")), Math.max(1, GsonHelper.getAsInt(obj, "quantity", 1)));
	}

	/**
	 * Test the ItemStack using ingredient. Does not count quantity.
	 */
	@Override public boolean test(ItemStack itemStack){
		return ingredient.test(itemStack);
	}

	public JsonElement serialize(){
		JsonObject obj = new JsonObject();
		obj.add("ingredient", ingredient.toJson());
		if(quantity!=1) obj.addProperty("quantity", quantity);
		return obj;
	}

	public void write(FriendlyByteBuf buffer){
		ingredient.toNetwork(buffer);
		buffer.writeVarInt(quantity);
	}
}
