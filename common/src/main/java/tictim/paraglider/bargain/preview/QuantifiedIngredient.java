package tictim.paraglider.bargain.preview;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.bargain.DemandPreview;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Pair of an ingredient and a non-negative int.
 */
public record QuantifiedIngredient(
		@NotNull Ingredient ingredient,
		@Range(from = 0, to = Integer.MAX_VALUE) int quantity
) implements Predicate<ItemStack>, DemandPreview{
	@NotNull public static QuantifiedIngredient read(@NotNull FriendlyByteBuf buffer){
		return new QuantifiedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt());
	}

	public QuantifiedIngredient(@NotNull Ingredient ingredient, int quantity){
		this.ingredient = Objects.requireNonNull(ingredient);
		this.quantity = Math.max(0, quantity);
	}

	@NotNull public static final Codec<QuantifiedIngredient> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(QuantifiedIngredient::ingredient),
			Codec.INT.fieldOf("quantity").forGetter(QuantifiedIngredient::quantity)
		).apply(instance, QuantifiedIngredient::new)
	);

	/**
	 * Test the ItemStack using ingredient. Does not count quantity.
	 */
	@Override public boolean test(ItemStack itemStack){
		return ingredient.test(itemStack);
	}

	@Override @NotNull @Unmodifiable public List<@NotNull ItemStack> preview(){
		return List.of(ingredient.getItems());
	}
	@Override public int count(@NotNull Player player){
		int count = 0;
		for(int i = 0; i<player.getInventory().getContainerSize(); i++){
			ItemStack stack = player.getInventory().getItem(i);
			if(stack.isEmpty()||!ingredient.test(stack)) continue;
			count += stack.getCount();
		}
		return count;
	}
	@Override @NotNull public List<@NotNull Component> getTooltip(int previewIndex){
		ItemStack[] items = ingredient.getItems();
		if(items.length==0) return List.of();
		ItemStack stack = items[previewIndex<0||items.length<=previewIndex ? 0 : previewIndex];
		return Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
	}

	@NotNull public JsonElement serialize(){
		JsonObject obj = new JsonObject();
		obj.add("ingredient", ingredient.toJson(false));
		if(quantity!=1) obj.addProperty("quantity", quantity);
		return obj;
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		ingredient.toNetwork(buffer);
		buffer.writeVarInt(quantity);
	}
}
