package tictim.paraglider.contents.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpiritOrbItem extends Item{
	public SpiritOrbItem(Properties properties){
		super(properties);
	}

	@Override public boolean isFoil(ItemStack stack){
		return true;
	}
}
