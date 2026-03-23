package tictim.paraglider.contents.item.consumeeffect;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.Stamina;

@NullMarked
public record RestoreStaminaConsumeEffect(
		double amount
) implements ConsumeEffect {
	public static final Type<RestoreStaminaConsumeEffect> TYPE = new Type<>(
			Codec.doubleRange(Mth.EPSILON, Double.MAX_VALUE)
					.xmap(RestoreStaminaConsumeEffect::new, RestoreStaminaConsumeEffect::amount)
					.fieldOf("amount"),
			ByteBufCodecs.DOUBLE.map(RestoreStaminaConsumeEffect::new, RestoreStaminaConsumeEffect::amount).cast()
	);

	@Override public Type<? extends ConsumeEffect> getType() {
		return TYPE;
	}

	@Override public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player player) {
			return Stamina.get(player).giveStamina(this.amount, false) > 0;
		}
		return false;
	}
}
