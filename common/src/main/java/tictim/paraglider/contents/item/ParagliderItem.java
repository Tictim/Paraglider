package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.item.Paraglider;

import java.util.List;

public abstract class ParagliderItem extends Item implements DyeableLeatherItem, Paraglider{
	private final int defaultColor;

	public ParagliderItem(int defaultColor){
		super(new Properties().stacksTo(1));
		this.defaultColor = defaultColor;
	}

	@Override public int getColor(@NotNull ItemStack stack){
		CompoundTag nbt = stack.getTagElement("display");
		return nbt!=null&&nbt.contains("color", 99) ? nbt.getInt("color") : defaultColor;
	}

	@Override public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag){
		if(stack.isDamageableItem()&&stack.getMaxDamage()<=stack.getDamageValue()){
			tooltip.add(Component.translatable("tooltip.paraglider.paraglider_broken").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}

	@Override public boolean canDoParagliding(@NotNull ItemStack stack){
		return !(stack.isDamageableItem()&&stack.getMaxDamage()<=stack.getDamageValue());
	}
	@Override public boolean isParagliding(@NotNull ItemStack stack){
		CompoundTag tag = stack.getTag();
		return tag!=null&&tag.getBoolean("Paragliding");
	}

	@Override public void setParagliding(@NotNull ItemStack stack, boolean paragliding){
		if(isParagliding(stack)==paragliding) return;
		CompoundTag tag = stack.getOrCreateTag();
		if(paragliding) tag.putBoolean("Paragliding", true);
		else tag.remove("Paragliding");
	}

	@Override public void damageParaglider(@NotNull Player player, @NotNull ItemStack stack){
		ParagliderUtils.damageItemWithoutBreaking(player, stack);
	}
}
