package tictim.paraglider.api.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import tictim.paraglider.api.ParagliderAPI;

/**
 * Interface
 */
public interface Movement{
	@NotNull static Movement get(@NotNull Player player){
		return ParagliderAPI.movementSupplier().apply(player);
	}

	/**
	 * @return Current state of this movement instance
	 */
	@NotNull PlayerState state();

	@Range(from = 0, to = Integer.MAX_VALUE) int recoveryDelay();

	void setRecoveryDelay(int recoveryDelay);
}
