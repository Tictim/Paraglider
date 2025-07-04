package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.config.Cfg;

import java.util.List;
import java.util.function.Consumer;

public class ParagliderItem extends Item {
	public ParagliderItem(Properties p) {
		super(p.durability(100));
	}

	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
		if (stack.isDamaged() && stack.getMaxDamage() <= stack.getDamageValue()) {
			tooltipComponents.add(Component.translatable("tooltip.paraglider.paraglider_broken")
					.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}

	@Override public int getMaxDamage(@NotNull ItemStack stack) {
		return Math.max(1, Cfg.get().paragliderDurability());
	}

	@Override public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, @Nullable T entity, @NotNull Consumer<Item> onBroken) {
		return Cfg.get().paragliderDurability() > 0 ? amount : 0;
	}

	@Override public boolean canGrindstoneRepair(@NotNull ItemStack stack) {
		return false;
	}

	@Override public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack repairCandidate) {
		return repairCandidate.is(Tags.Items.LEATHERS);
	}

	@Override public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
		return slotChanged || super.shouldCauseBlockBreakReset(oldStack, newStack); // checks everything other than damage
	}
}
