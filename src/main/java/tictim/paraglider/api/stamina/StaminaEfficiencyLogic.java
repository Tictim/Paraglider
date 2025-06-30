package tictim.paraglider.api.stamina;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.PlayerState;

/**
 * <p>
 * Instance representation of stamina efficiency logic.
 * </p>
 * <p>
 * Stamina efficiency is a numeric value that applies proportional change to base stamina delta. The system is only
 * used for stamina delta of movement states in base mod, but it can also be used in external systems, possibly with
 * special type of {@link Context}.
 * </p>
 * <p>
 * Stamina efficiency's logic behaves differently to negative and positive base stamina delta: Positive stamina
 * efficiency increases positive delta, while decreasing negative delta. Thus, positive stamina efficiency will always
 * have positive effect, regardless of the sign of base stamina delta; Negative efficiency works in the opposite way -
 * decreasing positive delta, and increasing negative delta. Efficiency of {@code 0}, as well as {@code NaN}, does not
 * affect base stamina delta.
 * </p>
 * <p>
 * The function used for stamina efficiency also depends on the direction of the change - if the efficiency decreases
 * the base stamina delta's magnitude (move towards zero; negative base delta and positive efficiency,
 * for example) it uses the function below:
 * <pre>d / (1 + |e|)</pre>
 * with {@code d} as stamina delta, and {@code e} as efficiency. One way to think about this function is that it alters
 * stamina delta based on <i>how long</i> it takes to change stamina by specific amount; if a base delta takes time
 * {@code T} to produce set amount of cumulative change, with efficiency of {@code 1} it will take {@code 2T} to
 * produce the same cumulative change, and {@code 3T} with efficiency of {@code 2}, and so on.
 * </p>
 * <p>
 * If the efficiency increases the base stamina delta's magnitude (move away from zero; negative base delta and negative
 * efficiency, for example) it uses the function below:
 * <pre>d * (1 + |e|)</pre>
 * with {@code d} as stamina delta, and {@code e} as efficiency.
 * </p>
 * <p>
 * Each instance of efficiency logic is checked and evaluated for each efficiency calculation. The sum of individual
 * efficiency values is used for the efficiency function's input.
 * </p>
 * <p>
 * For base mod's movement delta, stamina efficiency is evaluated each tick on server-side. Then, if the value differs
 * from last tick, it is synced to the client.
 * </p>
 * <p>
 * For registering custom logic, see
 * {@link StaminaPlugin#registerStaminaEfficiencyLogic(StaminaPlugin.StaminaEfficiencyLogicRegister)}.
 * </p>
 *
 * @see PlayerState
 */
public interface StaminaEfficiencyLogic {
	static @NotNull StaminaEfficiencyLogicHandler handler() {
		return ParagliderAPI.staminaEfficiencyLogicHandler();
	}

	/**
	 * Standalone function for applying efficiency logic using specific stamina delta and efficiency.
	 *
	 * @param baseStaminaDelta Base stamina delta
	 * @param efficiency       Efficiency
	 * @return Modified stamina delta, or same value as {@code baseStaminaDelta} if
	 * {@code efficiency == 0 || Double.isNaN(efficiency)}
	 */
	static double applyEfficiency(double baseStaminaDelta, double efficiency) {
		if (efficiency == 0 || Double.isNaN(efficiency)) return baseStaminaDelta;

		boolean sd = baseStaminaDelta > 0;
		boolean se = efficiency > 0;
		double factor = 1 + Math.abs(efficiency);

		return sd == se ? baseStaminaDelta * factor : baseStaminaDelta / factor;
	}

	/**
	 * <p>
	 * Return whether this logic is applicable for given context. If this method returns {@code false},
	 * the logic is excluded from evaluation.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param baseStaminaDelta Base stamina delta, not {@code 0} nor {@code NaN} in most circumstances.
	 * @param context          Context
	 * @return Whether the logic is applicable
	 */
	boolean isApplicable(double baseStaminaDelta, @NotNull Context context);

	/**
	 * <p>
	 * Evaluate stamina efficiency with given context. Return value of {@code NaN} will be ignored.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param baseStaminaDelta Base stamina delta, not {@code 0} nor {@code NaN} in most circumstances.
	 * @param context          Context
	 * @return Efficiency
	 */
	double getEfficiency(double baseStaminaDelta, @NotNull Context context);

	/**
	 * Context for stamina efficiency calculations.
	 */
	interface Context {
		@Nullable Player player();
		@Nullable PlayerState state();

		/**
		 * A utility method to check the ID of a state.
		 *
		 * @param id ID
		 * @return Whether {@link #state()} is not null and has given ID
		 */
		default boolean stateIs(@NotNull ResourceLocation id) {
			PlayerState state = state();
			return state != null && state.is(id);
		}

		/**
		 * A utility method to check the flag of a state.
		 *
		 * @param flag Flag
		 * @return Whether {@link #state()} is not null and has given flag
		 */
		default boolean stateHasFlag(@NotNull ResourceLocation flag) {
			PlayerState state = state();
			return state != null && state.hasFlag(flag);
		}
	}

	record SimpleContext(
			@Nullable Player player,
			@Nullable PlayerState state
	) implements StaminaEfficiencyLogic.Context {}
}
