package tictim.paraglider.contents.item.consumeeffect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.Contents;

public record GiveStaminaEfficiencyConsumeEffect(
		int level,
		int duration
) implements ConsumeEffect {
	public static final Type<GiveStaminaEfficiencyConsumeEffect> TYPE = new Type<>(
			RecordCodecBuilder.mapCodec(b -> b.group(
					Codec.INT.fieldOf("level").forGetter(GiveStaminaEfficiencyConsumeEffect::level),
					Codec.INT.fieldOf("duration").forGetter(GiveStaminaEfficiencyConsumeEffect::duration)
			).apply(b, GiveStaminaEfficiencyConsumeEffect::new)),
			StreamCodec.composite(
					ByteBufCodecs.INT, GiveStaminaEfficiencyConsumeEffect::level,
					ByteBufCodecs.INT, GiveStaminaEfficiencyConsumeEffect::duration,
					GiveStaminaEfficiencyConsumeEffect::new
			)
	);

	@Override public @NotNull Type<? extends ConsumeEffect> getType() {
		return TYPE;
	}

	@Override public boolean apply(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity entity) {
		return entity.addEffect(new MobEffectInstance(Contents.get().staminaEfficiencyEffect, this.duration, this.level));
	}
}
