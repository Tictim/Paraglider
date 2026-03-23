package tictim.paraglider.impl.stamina;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.util.Objects;
import java.util.function.Supplier;

@NullMarked
public record AttributeStaminaEfficiencyLogic(
		Supplier<Holder<Attribute>> attribute,
		StaminaPlugin.AttributeEfficiencyCondition condition
) implements StaminaEfficiencyLogic {
	@Override public boolean isApplicable(double baseStaminaDelta, Context context) {
		Player p = context.player();
		if (p == null) return false;

		AttributeInstance a = p.getAttribute(this.attribute.get());
		if (a == null) return false;

		return this.condition.isApplicable(baseStaminaDelta, context, p);
	}

	@Override public double getEfficiency(double baseStaminaDelta, Context context) {
		return Objects.requireNonNull(context.player()).getAttributeValue(this.attribute.get()) - 1;
	}
}
