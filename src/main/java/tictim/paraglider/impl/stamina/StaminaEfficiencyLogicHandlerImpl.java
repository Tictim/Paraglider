package tictim.paraglider.impl.stamina;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogicHandler;

import java.util.List;
import java.util.Objects;

@NullMarked
public final class StaminaEfficiencyLogicHandlerImpl implements StaminaEfficiencyLogicHandler {
	private final List<StaminaEfficiencyLogic> logics;

	public StaminaEfficiencyLogicHandlerImpl(@Unmodifiable List<StaminaEfficiencyLogic> logics) {
		this.logics = logics;
	}

	public @Unmodifiable List<StaminaEfficiencyLogic> logics() {
		return this.logics;
	}

	@Override public double getEfficiencySum(double baseStaminaDelta, StaminaEfficiencyLogic.Context context) {
		if (baseStaminaDelta == 0 || Double.isNaN(baseStaminaDelta)) return baseStaminaDelta;
		Objects.requireNonNull(context, "context == null");

		double sum = 0;

		for (StaminaEfficiencyLogic logic : logics()) {
			if (!logic.isApplicable(baseStaminaDelta, context)) continue;

			double value = logic.getEfficiency(baseStaminaDelta, context);
			if (!Double.isNaN(value)) sum += value;
		}

		return Double.isNaN(sum) ? 0 : sum;
	}
}
