package tictim.paraglider.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.item.ParagliderItem;

public final class ParagliderItemColor implements ItemColor{
	@NotNull private final ParagliderItem item;

	public ParagliderItemColor(@NotNull ParagliderItem item){
		this.item = item;
	}

	@Override public int getColor(ItemStack stack, int tint){
		return tint>0 ? -1 : item.getColor(stack);
	}
}
