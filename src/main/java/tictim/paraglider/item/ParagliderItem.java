package tictim.paraglider.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.List;

public class ParagliderItem extends Item implements DyeableLeatherItem{
	private final int defaultColor;

	public ParagliderItem(int defaultColor){
		super(new Properties().stacksTo(1).tab(Contents.GROUP));
		this.defaultColor = defaultColor;
	}

	@Override public int getMaxDamage(ItemStack stack){
		return ModCfg.paragliderDurability();
	}
	@Override public boolean canBeDepleted(){
		return ModCfg.paragliderDurability()>0;
	}
	@Override public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair){
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		return tags!=null&&tags.getTag(Tags.Items.LEATHER).contains(repair.getItem());
	}

	@Nullable @Override public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt){
		return new Paraglider();
	}

	@Override public int getColor(ItemStack stack){
		CompoundTag nbt = stack.getTagElement("display");
		return nbt!=null&&nbt.contains("color", 99) ? nbt.getInt("color") : defaultColor;
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag){
		if(stack.isDamageableItem()&&stack.getMaxDamage()<=stack.getDamageValue()){
			tooltip.add(new TranslatableComponent("tooltip.paraglider.paraglider_broken").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}

	@Override public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){
		return slotChanged||oldStack.getItem()!=newStack.getItem()||isItemParagliding(oldStack)!=isItemParagliding(newStack);
	}

	public static boolean isItemParagliding(ItemStack stack){
		CompoundTag tag = stack.getTag();
		return tag!=null&&tag.getBoolean("Paragliding");
	}

	public static void setItemParagliding(ItemStack stack, boolean paragliding){
		if(isItemParagliding(stack)==paragliding) return;
		CompoundTag tag = stack.getOrCreateTag();
		if(paragliding) tag.putBoolean("Paragliding", true);
		else tag.remove("Paragliding");
	}
}
