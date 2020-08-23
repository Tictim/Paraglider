package tictim.paraglider.item;

import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;

public class ParagliderItem extends Item implements IDyeableArmorItem{
	public static boolean hasParaglidingFlag(ItemStack stack){
		Paraglider p = stack.getCapability(Paraglider.CAP).orElse(null);
		return p!=null&&p.isParagliding;
	}

	private final int defaultColor;

	public ParagliderItem(int defaultColor){
		super(new Properties().maxStackSize(1).group(Contents.GROUP));
		this.defaultColor = defaultColor;
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt){
		return new Paraglider();
	}

	@Override
	public int getColor(ItemStack stack){
		CompoundNBT nbt = stack.getChildTag("display");
		return nbt!=null&&nbt.contains("color", 99) ? nbt.getInt("color") : defaultColor;
	}
}
