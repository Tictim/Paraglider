package tictim.paraglider.contents.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpiritOrbItem extends Item{
	public SpiritOrbItem(@NotNull Properties properties){
		super(properties);
	}

	@Override public boolean isFoil(@NotNull ItemStack stack){
		return true;
	}
}
