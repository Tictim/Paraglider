package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.mobeffect.StaminaEfficiencyMobEffect;

import java.text.DecimalFormat;
import java.util.List;

public class BaseElixirItem extends Item {
	private static final DecimalFormat D0 = new DecimalFormat("0.#");
	private static final DecimalFormat PCT = new DecimalFormat("+0%");

	public BaseElixirItem(Properties properties) {
		super(properties);
	}

	@Override public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
		player.startUsingItem(usedHand);
		return InteractionResultHolder.consume(player.getItemInHand(usedHand));
	}

	@Override public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
		return 12; // 0.6s
	}

	public static class Energizing extends BaseElixirItem {
		private final double staminaRecovered;
		private final int staminaEfficiencyLevel;
		private final int staminaEfficiencyDuration;

		public Energizing(Properties properties, double staminaRecovered) {
			this(properties, staminaRecovered, -1, 0);
		}

		public Energizing(Properties properties, double staminaRecovered, int staminaEfficiencyLevel, int staminaEfficiencyDuration) {
			super(properties);
			this.staminaRecovered = staminaRecovered;
			this.staminaEfficiencyLevel = staminaEfficiencyLevel;
			this.staminaEfficiencyDuration = staminaEfficiencyDuration;
		}

		@Override public @NotNull ItemStack finishUsingItem(
				@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
			if (livingEntity instanceof Player p) {
				Stamina stamina = Stamina.get(p);
				stamina.giveStamina(this.staminaRecovered, false);

				if (this.staminaEfficiencyLevel >= 0 && this.staminaEfficiencyDuration > 0) {
					p.addEffect(new MobEffectInstance(Contents.get().staminaEfficiencyEffect,
							this.staminaEfficiencyDuration, this.staminaEfficiencyLevel));
				}

				p.awardStat(Stats.ITEM_USED.get(this));
				stack.consume(1, p);

				if (!p.hasInfiniteMaterials()) {
					if (stack.isEmpty()) {
						return new ItemStack(Items.GLASS_BOTTLE);
					}

					p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
				}
			}

			livingEntity.gameEvent(GameEvent.DRINK);
			return stack;
		}

		@Override public void appendHoverText(
				@NotNull ItemStack stack, @NotNull TooltipContext context,
				@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {

			tooltipComponents.add(Component.translatable("tooltip.paraglider.give_extra_stamina",
					Component.literal(D0.format(this.staminaRecovered / Stamina.STAMINA_PER_WHEEL))
							.withStyle(ChatFormatting.YELLOW)
			).withStyle(ChatFormatting.GREEN));
			if (this.staminaEfficiencyLevel >= 0 && this.staminaEfficiencyDuration > 0) {
				tooltipComponents.add(Component.translatable("tooltip.paraglider.give_stamina_efficiency",
						Component.literal(PCT.format(
								StaminaEfficiencyMobEffect.EFFICIENCY_PER_LEVEL * (this.staminaEfficiencyLevel + 1)
						)).withStyle(ChatFormatting.YELLOW),
						Component.literal(D0.format(this.staminaEfficiencyDuration / 20.0)).withStyle(ChatFormatting.YELLOW)
				).withStyle(ChatFormatting.GREEN));
			}
		}
	}

	public static class Enduring extends BaseElixirItem {
		private final double extraStamina;

		public Enduring(Properties properties, double extraStamina) {
			super(properties);
			this.extraStamina = extraStamina;
		}

		@Override public void appendHoverText(
				@NotNull ItemStack stack, @NotNull TooltipContext context,
				@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
			tooltipComponents.add(Component.translatable("tooltip.paraglider.restore_stamina",
					Component.literal(D0.format(this.extraStamina / Stamina.STAMINA_PER_WHEEL))
							.withStyle(ChatFormatting.YELLOW)
			).withStyle(ChatFormatting.GREEN));
		}
	}
}
