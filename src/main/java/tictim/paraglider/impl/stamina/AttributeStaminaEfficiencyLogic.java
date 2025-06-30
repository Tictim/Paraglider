package tictim.paraglider.impl.stamina;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.util.Objects;
import java.util.function.Supplier;

public record AttributeStaminaEfficiencyLogic(
		@NotNull Supplier<Holder<Attribute>> attribute,
		StaminaPlugin.@NotNull AttributeEfficiencyCondition condition
) implements StaminaEfficiencyLogic {
	@Override public boolean isApplicable(double baseStaminaDelta, @NotNull Context context) {
		Player p = context.player();
		if (p == null) return false;

		AttributeInstance a = p.getAttribute(this.attribute.get());
		if (a == null) return false;

		return this.condition.isApplicable(baseStaminaDelta, context, p);
	}

	@Override public double getEfficiency(double baseStaminaDelta, @NotNull Context context) {
		return Objects.requireNonNull(context.player()).getAttributeValue(this.attribute.get()) - 1;
	}
}
