package tictim.paraglider.api;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.util.function.Supplier;

/**
 * Standard implementation for stamina efficiency attributes. Use {@link
 * net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADD_MULTIPLIED_BASE ADD_MULTIPLIED_BASE} for
 * all modifiers. Syncing is not a de facto requirement since stamina efficiency evaluation is handled entirely on
 * server-side.
 *
 * @see tictim.paraglider.api.stamina.StaminaPlugin.StaminaEfficiencyLogicRegister#registerAttribute(Supplier, StaminaPlugin.AttributeEfficiencyCondition)
 */
public class StaminaEfficiencyAttribute extends Attribute {
	public StaminaEfficiencyAttribute(String descriptionId) {
		// default value is "1" because it enables use of ADD_MULTIPLIED_BASE
		// the value is decremented by 1 on actual efficiency calculation
		super(descriptionId, 1);
	}

	@Override public @NotNull StaminaEfficiencyAttribute setSyncable(boolean watch) {
		super.setSyncable(watch);
		return this;
	}

	@Override public @NotNull StaminaEfficiencyAttribute setSentiment(@NotNull Sentiment sentiment) {
		super.setSentiment(sentiment);
		return this;
	}

	@Override public double sanitizeValue(double value) {
		return Double.isNaN(value) ? 0 : value;
	}
}
