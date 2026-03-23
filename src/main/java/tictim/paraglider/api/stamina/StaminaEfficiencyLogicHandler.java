package tictim.paraglider.api.stamina;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.movement.PlayerState;

/**
 * Handler for stamina efficiency evaluation
 */
@NullMarked
public interface StaminaEfficiencyLogicHandler {
	/**
	 * Compute efficiency sum with given context parameters.
	 *
	 * @param baseStaminaDelta Base stamina delta
	 * @param player           Player, optional
	 * @param state            State, optional
	 * @return Efficiency sum, or {@code baseStaminaDelta} if
	 * {@code baseStaminaDelta == 0 || Double.isNaN(baseStaminaDelta)}
	 */
	default double getEfficiencySum(double baseStaminaDelta, @Nullable Player player, @Nullable PlayerState state) {
		return getEfficiencySum(baseStaminaDelta, new StaminaEfficiencyLogic.SimpleContext(player, state));
	}

	/**
	 * Compute efficiency sum with given context.
	 *
	 * @param baseStaminaDelta Base stamina delta
	 * @param context          Context
	 * @return Efficiency sum, or {@code baseStaminaDelta} if
	 * {@code baseStaminaDelta == 0 || Double.isNaN(baseStaminaDelta)}
	 * @throws NullPointerException If {@code context == null}
	 */
	double getEfficiencySum(double baseStaminaDelta, StaminaEfficiencyLogic.Context context);
}
