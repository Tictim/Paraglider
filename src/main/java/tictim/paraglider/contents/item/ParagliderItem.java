package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.impl.movement.PlayerMovementValues;

import java.util.Optional;
import java.util.function.Consumer;

@NullMarked
public class ParagliderItem extends Item {
	public ParagliderItem(Properties p) {
		super(p
				.durability(100)
				.repairable(Tags.Items.LEATHERS)
				.component(DataComponents.USE_COOLDOWN,
						new UseCooldown(PlayerMovementValues.PARAGLIDER_ITEM_COOLDOWN / 20.f,
								Optional.of(ParagliderAPI.PARAGLIDER_COOLDOWN_GROUP))
				)
		);
	}

	@Override public InteractionResult use(
			Level level,
			Player player,
			InteractionHand hand) {
		if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

		if (level.isClientSide()) {
			if (Movement.get(player) instanceof ClientPlayerMovement m) {
				m.setAutoParagliding(false);
				if (m.clientParagliding()) m.stopUsingParaglider();
				else m.useParaglider();
			}
		}

		return InteractionResult.CONSUME;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			ItemStack stack, TooltipContext context,
			TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder,
			TooltipFlag flag) {
		if (stack.isDamaged() && stack.getMaxDamage() <= stack.getDamageValue()) {
			tooltipAdder.accept(Component.translatable("tooltip.paraglider.paraglider_broken")
					.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}

	@Override public int getMaxDamage(ItemStack stack) {
		return Cfg.get().paragliderDurability();
	}

	@Override public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
		return Cfg.get().paragliderDurability() > 0 ? amount : 0;
	}

	@Override public boolean canGrindstoneRepair(ItemStack stack) {
		return false;
	}

	@Override public boolean isCombineRepairable(ItemStack stack) {
		return false;
	}

	@Override public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || super.shouldCauseBlockBreakReset(oldStack, newStack); // checks everything other than damage
	}
}
