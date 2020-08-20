package tictim.paraglider.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SpiritOrbItem extends Item{
	public SpiritOrbItem(Properties properties){
		super(properties);
	}

	@Override public boolean hasEffect(ItemStack stack){
		return true;
	}
}
