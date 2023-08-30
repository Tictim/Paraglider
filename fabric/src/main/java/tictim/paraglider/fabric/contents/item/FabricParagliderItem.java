package tictim.paraglider.fabric.contents.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.item.ParagliderItem;

public class FabricParagliderItem extends ParagliderItem implements FabricItem{
	public FabricParagliderItem(int defaultColor){
		super(defaultColor);
	}

	@Override public int getMaxDamage(){
		return Cfg.get().paragliderDurability();
	}
	@Override public boolean canBeDepleted(){
		return Cfg.get().paragliderDurability()>0;
	}

	@Override public int getBarWidth(@NotNull ItemStack stack){
		return Math.round(13f-stack.getDamageValue()*13f/Cfg.get().paragliderDurability());
	}

	@Override public int getBarColor(@NotNull ItemStack stack){
		float durability = Cfg.get().paragliderDurability();
		return Mth.hsvToRgb(Math.max(0, (durability-stack.getDamageValue())/durability)/3, 1, 1);
	}

	@Override public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair){
		return repair.getItem()==Items.LEATHER;
	}

	@Override public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack){
		return oldStack.getItem()!=newStack.getItem()||isParagliding(oldStack)!=isParagliding(newStack);
	}
}
