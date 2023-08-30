package tictim.paraglider.api;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;

/**
 * Interface providing method for copying properties between instances. If {@link Movement}, {@link VesselContainer}
 * or {@link Stamina} implements this, the game will use the method to transfer any property between different instances
 * of each type, in case where re-instantiation is necessary (e.g. player entity respawn, dimension change)
 */
public interface Copy{
	/**
	 * Copy properties from {@code from} to {@code this}. It's possible to receive incompatible object for copy
	 * operation; in the case the copy operation should finish without producing error.
	 *
	 * @param from Object to copy properties from
	 */
	void copyFrom(@NotNull Object from);
}
