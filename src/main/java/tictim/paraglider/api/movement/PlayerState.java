package tictim.paraglider.api.movement;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.ParagliderPlayerStates.Flags;

import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * Defines a specific state of player's movement. Paraglider's stamina consumption/regeneration is based on player
 * state, which is evaluated and synced from server to clients, each tick.
 * </p>
 * <p>
 * To register new player states, see {@link MovementPlugin}.
 * </p>
 */
public interface PlayerState {
	/**
	 * @return ID of the state
	 */
	@NotNull ResourceLocation id();

	/**
	 * @return Flag of the state
	 * @see Flags
	 */
	@NotNull @Unmodifiable Set<@NotNull ResourceLocation> flags();

	/**
	 * @return Stamina delta of the state; positive values indicate this state replenishes stamina, negative values
	 * indicate this state consumes stamina, and {@code 0} indicates this state is stamina-neutral.
	 */
	double staminaDelta();

	/**
	 * @return <p>
	 * Recovery delay of this player state; when the player is in this state, the recovery delay will be set to
	 * this value. Recovery delay is a persistent number that goes down by 1 each tick that prevents stamina
	 * generation when the value is positive.
	 * </p>
	 * <p>
	 * Normally this value corresponds to {@link #staminaDelta()} - the recovery delay is expected to be {@code 0} if
	 * stamina delta is 0 or positive, and some positive value (mostly {@link ParagliderPlayerStates#RECOVERY_DELAY})
	 * if stamina delta is negative. Using positive recovery delay with positive stamina delta will probably do some
	 * funny, so please don't.
	 * </p>
	 */
	@Range(from = 0, to = Integer.MAX_VALUE) int recoveryDelay();

	/**
	 * Check if this state has given ID.
	 *
	 * @param id ID
	 * @return Whether this state has given ID
	 */
	default boolean is(@NotNull ResourceLocation id) {
		Objects.requireNonNull(id, "id == null");
		return id().equals(id);
	}

	/**
	 * Check if this state has given flag.
	 *
	 * @param flag Flag
	 * @return Whether this state has given flag
	 */
	default boolean hasFlag(@NotNull ResourceLocation flag) {
		Objects.requireNonNull(flag, "flag == null");
		return flags().contains(flag);
	}

	/**
	 * @return Whether this state has the flag {@link Flags#PARAGLIDING}
	 */
	default boolean paragliding() {
		return hasFlag(Flags.PARAGLIDING);
	}
}
