package tictim.paraglider.api.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Instance representation of stamina reduction logic.
 * </p>
 * <p>
 * Stamina reduction rate is a proportion of change to be made to stamina delta. Function of stamina reduction rate
 * changes based on the sign of original stamina delta - positive reduction rate increases stamina regeneration on
 * positive stamina delta (+N%), and decreases stamina consumption on negative stamina delta (-N%). For example,
 * reduction rate of {@code 0.5} corresponds to +-50%, which could either increase stamina regeneration by half (+50%),
 * or reduce stamina consumption by half (-50%). Negative reduction rate will do the opposite. Note that the system
 * cannot make stamina delta positive from negative, or vice versa; reduction rate below -100% will just set stamina
 * delta to 0.
 * </p>
 * <p>
 * Stamina reduction is evaluated each tick on server-side. On evaluation, each registered stamina reduction logic is
 * checked for computing final reduction value. The exact steps are:
 * <ol>
 *    <li>Applicability for each individual logics are checked. Logics that return {@code false} on
 *    {@link #isApplicable(Player, PlayerState)} are applicable are excluded from subsequent steps.</li>
 *    <li>Reduction rate of individual logics are computed with {@link #getReductionRate(Player, PlayerState)}.</li>
 *    <li>The individual values are then summed together, and clamped to make one final reduction value. The minimum
 *    reduction value is the lowest value of {@link #getMinReduction(Player, PlayerState)} among all active logics, and
 *    the maximum reduction is the highest value of {@link #getMaxReduction(Player, PlayerState)} among all active
 *    logics.</li>
 * </ol>
 * The stamina reduction value is automatically synced to the client.
 * </p>
 * <p>
 * For registering custom logic, see
 * {@link MovementPlugin#registerStaminaReductionLogic(MovementPlugin.StaminaReductionLogicRegister)}.
 * </p>
 *
 * @see PlayerState
 */
public interface StaminaReductionLogic{
	/**
	 * <p>
	 * Return whether this logic is applicable for given player and player state. If this method returns {@code false},
	 * the logic is excluded from evaluation.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param player Player
	 * @param state  Player state
	 * @return Whether the stamina reduction is applicable
	 */
	boolean isApplicable(@NotNull Player player, @NotNull PlayerState state);

	/**
	 * <p>
	 * Get a reduction rate of the player state based on the player's state. The reduction rate is a proportion of
	 * change to be made to stamina delta; for example, reduction rate of {@code 0.5} corresponds to -50% to stamina
	 * delta, which could either reduce stamina consumption by half, or reduce stamina regeneration by half. When
	 * multiple stamina reduction function returns a nonzero value, the values will be added together. Return value of
	 * {@code NaN} is ignored.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param player Player
	 * @param state  Player state
	 * @return Reduction rate of the player state
	 */
	double getReductionRate(@NotNull Player player, @NotNull PlayerState state);

	/**
	 * <p>
	 * Get minimum reduction value for this stamina reduction logic. The final reduction value will be clamped with the
	 * return value. Note that the final reduction value could go below specified rate, if another logic provides lower
	 * minimum value. Return value of {@code NaN} is ignored.
	 * </p>
	 * <p>
	 * The default implementation has safeguard for preventing positive stamina delta to go down to 0.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param player Player
	 * @param state  Player state
	 * @return Minimum reduction value
	 */
	default double getMinReduction(@NotNull Player player, @NotNull PlayerState state){
		// just to prevent unintentional implementations allowing positive stamina delta go down to 0.
		// if you want to allow it, override this method.
		return state.staminaDelta()>0 ? -0.8 : Double.NEGATIVE_INFINITY;
	}

	/**
	 * <p>
	 * Get maximum reduction value for this stamina reduction logic. The final reduction value will be clamped with the
	 * return value. Note that the final reduction value could go above specified rate, if another logic provides higher
	 * maximum value. Return value of {@code NaN} is ignored.
	 * </p>
	 * <p>
	 * The default implementation has safeguard for preventing negative stamina delta to go up to 0.
	 * </p>
	 * <p>
	 * This method is only called on server-side.
	 * </p>
	 *
	 * @param player Player
	 * @param state  Player state
	 * @return Maximum reduction value
	 */
	default double getMaxReduction(@NotNull Player player, @NotNull PlayerState state){
		// just to prevent unintentional implementations allowing negative stamina delta go up to 0.
		// if you want to allow it, override this method.
		return state.staminaDelta()<0 ? 0.8 : Double.POSITIVE_INFINITY;
	}
}
