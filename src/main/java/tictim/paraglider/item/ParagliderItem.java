package tictim.paraglider.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.List;

public class ParagliderItem extends Item implements IDyeableArmorItem{
	private final int defaultColor;

	public ParagliderItem(int defaultColor){
		super(new Properties().maxStackSize(1).group(Contents.GROUP));
		this.defaultColor = defaultColor;
	}

	@Override public int getMaxDamage(ItemStack stack){
		return ModCfg.paragliderDurability();
	}
	@Override public boolean isDamageable(){
		return ModCfg.paragliderDurability()>0;
	}
	@Override public boolean getIsRepairable(ItemStack toRepair, ItemStack repair){
		return Tags.Items.LEATHER.contains(repair.getItem());
	}

	@Nullable @Override public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt){
		return new Paraglider();
	}

	@Override public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected){
		if(world.isRemote) return;
		boolean paragliding;
		if(isSelected){
			ServerPlayerMovement m = ServerPlayerMovement.of(entity);
			paragliding = m!=null&&m.isParagliding();
		}else paragliding = false;
		setItemParagliding(stack, paragliding);
	}

	@Override public boolean updateItemStackNBT(CompoundNBT nbt){
		return super.updateItemStackNBT(nbt);
	}
	@Override public int getColor(ItemStack stack){
		CompoundNBT nbt = stack.getChildTag("display");
		return nbt!=null&&nbt.contains("color", 99) ? nbt.getInt("color") : defaultColor;
	}

	@Override public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag){
		if(stack.getMaxDamage()<=stack.getDamage()){
			tooltip.add(new TranslationTextComponent("tooltip.paraglider.paraglider_broken").setStyle(Style.EMPTY.setFormatting(TextFormatting.RED)));
		}
	}

	@Override public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){
		return slotChanged||oldStack.getItem()!=newStack.getItem()||isItemParagliding(oldStack)!=isItemParagliding(newStack);
	}

	public static boolean isItemParagliding(ItemStack stack){
		CompoundNBT tag = stack.getTag();
		return tag!=null&&tag.getBoolean("Paragliding");
	}

	public static void setItemParagliding(ItemStack stack, boolean paragliding){
		if(isItemParagliding(stack)==paragliding) return;
		CompoundNBT tag = stack.getOrCreateTag();
		if(paragliding) tag.putBoolean("Paragliding", true);
		else tag.remove("Paragliding");
	}
}
