package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.config.Cfg;

import java.util.function.Consumer;

public class ParagliderItem extends Item {
	public ParagliderItem(Properties p) {
		super(p.durability(100).repairable(Tags.Items.LEATHERS));
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
			@NotNull TooltipFlag flag) {
		if (stack.isDamaged() && stack.getMaxDamage() <= stack.getDamageValue()) {
			tooltipAdder.accept(Component.translatable("tooltip.paraglider.paraglider_broken")
					.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}

	@Override public int getMaxDamage(@NotNull ItemStack stack) {
		return Cfg.get().paragliderDurability();
	}

	@Override public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
		return Cfg.get().paragliderDurability() > 0 ? amount : 0;
	}

	@Override public boolean canGrindstoneRepair(@NotNull ItemStack stack) {
		return false;
	}

	@Override public boolean isCombineRepairable(@NotNull ItemStack stack) {
		return false;
	}

	@Override public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || super.shouldCauseBlockBreakReset(oldStack, newStack); // checks everything other than damage
	}
}
