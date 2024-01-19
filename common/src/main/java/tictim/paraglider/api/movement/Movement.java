package tictim.paraglider.api.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import tictim.paraglider.api.ParagliderAPI;

/**
 * Interface providing access to movement state of the player.
 */
public interface Movement{
	/**
	 * Get a movement instance bound to specific player; if there's none, a no-op singleton implementation is returned.
	 *
	 * @param player Player
	 * @return A movement instance bound to specific player, or a no-op singleton implementation
	 */
	@NotNull static Movement get(@NotNull Player player){
		return ParagliderAPI.movementSupplier().apply(player);
	}

	/**
	 * @return Current state of this movement instance
	 */
	@NotNull PlayerState state();

	/**
	 * @return Recovery delay, in ticks; state-based stamina regeneration will not be applied when this value is greater
	 * than 0. Decreases each tick.
	 */
	@Range(from = 0, to = Integer.MAX_VALUE) int recoveryDelay();

	/**
	 * @param recoveryDelay Recovery delay to be set, in ticks; state-based stamina regeneration will not be applied
	 *                      when this value is greater than 0. Decreases each tick.
	 */
	void setRecoveryDelay(int recoveryDelay);

	/**
	 * @return Stamina reduction rate
	 */
	double staminaReductionRate();

	/**
	 * @return Actual stamina delta based on {@link #state() player state} and
	 * {@link #staminaReductionRate() reduction rate}
	 */
	int getActualStaminaDelta();
}
