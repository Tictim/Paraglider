package tictim.paraglider.bargain.preview;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.OfferPreview;

import java.util.List;
import java.util.Objects;

/**
 * Pair of an item and an unsigned int. Isn't it called {@link ItemStack}? Sure, but it ain't have support for
 * {@code 2^31-1} items.
 */
public record QuantifiedItem(@NotNull ItemStack item, int quantity) implements OfferPreview{
	@NotNull public static QuantifiedItem read(@NotNull FriendlyByteBuf buffer){
		return new QuantifiedItem(buffer.readItem(), buffer.readVarInt());
	}

	public QuantifiedItem(@NotNull Item item, int quantity){
		this(new ItemStack(item), Math.max(0, quantity));
	}

	public QuantifiedItem(@NotNull ItemStack item, int quantity){
		this.item = Objects.requireNonNull(item);
		this.quantity = Math.max(0, quantity);
	}

	@NotNull public static final Codec<QuantifiedItem> CODEC = ItemStack.CODEC.xmap(
		itemStack -> new QuantifiedItem(itemStack, itemStack.getCount()),
		QuantifiedItem::item);

	@Override @NotNull public ItemStack preview(){
		return item;
	}
	@Environment(EnvType.CLIENT)
	@Override @NotNull public List<@NotNull Component> getTooltip(){
		return Screen.getTooltipFromItem(Minecraft.getInstance(), item);
	}

	@NotNull public ItemStack getItemWithQuantity(){
		ItemStack copy = item.copy();
		copy.setCount(quantity);
		return copy;
	}

	@NotNull public JsonObject serialize(){
		JsonObject obj = new JsonObject();
		obj.addProperty("item", ParagliderUtils.getKey(item.getItem())+"");
		if(quantity!=1) obj.addProperty("count", quantity);
		return obj;
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeItem(item);
		buffer.writeVarInt(quantity);
	}
}
