package tictim.paraglider.impl;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.item.Paraglider;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParaglidingFlag;

public class DefaultParagliderItemCapability implements Paraglider {
	@Override public boolean canDoParagliding(@NotNull ItemStack stack) {
		return !stack.isDamageableItem() || stack.getMaxDamage() > stack.getDamageValue();
	}

	@Override public boolean isParagliding(@NotNull ItemStack stack) {
		return stack.get(Contents.get().paraglidingFlagComponent()) != null;
	}

	@Override public void setParagliding(@NotNull ItemStack stack, boolean paragliding) {
		DataComponentType<ParaglidingFlag> type = Contents.get().paraglidingFlagComponent();
		boolean flag = stack.get(type) != null;

		if (flag == paragliding) return;
		else if (paragliding) stack.set(type, ParaglidingFlag.INSTANCE);
		else stack.remove(type);
	}

	@Override public void damageParaglider(@NotNull Player player, @NotNull ItemStack stack) {
		ParagliderUtils.damageItemWithoutBreaking(player, stack);
	}
}
