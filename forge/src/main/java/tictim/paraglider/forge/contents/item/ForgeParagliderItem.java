package tictim.paraglider.forge.contents.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.item.ParagliderItem;

public class ForgeParagliderItem extends ParagliderItem{
	public ForgeParagliderItem(int defaultColor){
		super(defaultColor);
	}

	@Override public int getMaxDamage(@NotNull ItemStack stack){
		return Cfg.get().paragliderDurability();
	}
	@Override public boolean canBeDepleted(){
		return Cfg.get().paragliderDurability()>0;
	}
	@Override public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair){
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		return tags!=null&&tags.getTag(Tags.Items.LEATHER).contains(repair.getItem());
	}

	@Override public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged){
		return slotChanged||oldStack.getItem()!=newStack.getItem()||isParagliding(oldStack)!=isParagliding(newStack);
	}
}
