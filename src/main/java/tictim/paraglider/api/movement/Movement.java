package tictim.paraglider.api.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;

/**
 * Interface providing access to movement state of the player.
 */
@NullMarked
public interface Movement {
	/**
	 * Get a movement instance bound to the player.
	 *
	 * @param player Player
	 * @return A movement instance bound to the player
	 */
	static Movement get(Player player) {
		return ParagliderAPI.movementSupplier().apply(player);
	}

	/**
	 * @return Current state of this movement instance
	 */
	PlayerState state();

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
	 * @return Stamina delta currently being applied by this movement instance.
	 */
	double staminaDelta();
}
