package tictim.paraglider.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderItemCapability;
import tictim.paraglider.contents.Contents;

public class DefaultParagliderItemCapability implements ParagliderItemCapability {
	@Override public boolean canDoParagliding(@NotNull Player player, @NotNull ItemStack stack) {
		return !stack.isDamaged() || stack.getMaxDamage() > stack.getDamageValue();
	}

	@Override public boolean isParagliding(@NotNull ItemStack stack) {
		return stack.get(Contents.get().paraglidingFlagComponent()) != null;
	}

	@Override public void setParagliding(@NotNull ItemStack stack, boolean paragliding) {
		DataComponentType<@NotNull Unit> type = Contents.get().paraglidingFlagComponent();
		boolean flag = stack.get(type) != null;

		if (flag == paragliding) return;
		else if (paragliding) stack.set(type, Unit.INSTANCE);
		else stack.remove(type);
	}

	@Override public void damageParaglider(@NotNull Player player, @NotNull ItemStack stack) {
		int prevCount = stack.getCount();
		stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
		if (stack.getCount() < prevCount) {
			stack.setCount(prevCount);
			stack.setDamageValue(stack.getMaxDamage());
		}
	}
}
