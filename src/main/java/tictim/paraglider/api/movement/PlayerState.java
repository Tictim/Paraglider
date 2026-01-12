package tictim.paraglider.api.movement;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.ParagliderPlayerStates.Flags;

import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * A specific state of a player. Player states are evaluated from the server, and synced to client. Each player can have
 * only one active state at a time.
 * </p>
 * <p>
 * Paraglider's stamina consumption/regeneration is done through player states - specifically,
 * {@link #staminaDelta()} and {@link #recoveryDelay()}. Both values can be modified either by paraglider plugin
 * targeting specific states, or a config file by users. List of existing player states and their stamina delta /
 * recovery delay properties are synced to client.
 * </p>
 * <p>
 * Evaluation of player state is done through player state connections, which forms a directional graph. You may think
 * of player state connections as a behavior tree, with support for recursion among connections. Connections have two
 * flavors:
 * <ul>
 *     <li>Conditional connections have an {@link PlayerStateCondition} instance associated, and is not considered as a
 *     connection unless the condition returns {@code true}. A state can have multiple conditional connections attached,
 *     both incoming and outgoing.</li>
 *     <li>Fallback connections do not have any conditions. A state may have zero to one outgoing fallback connection;
 *     trying to register multiple fallback connections from same state will either create a conflict, or overwrite one,
 *     depending on their priority value.</li>
 * </ul>
 * A player state evaluation starts from root state {@link ParagliderPlayerStates#IDLE paraglider:idle}, and traverses
 * each connections' condition for a match. If a match is found, then the current state is set to the connected state,
 * and resumes connection evaluation. It is possible for current state to re-visit previously visited state; in that
 * case, connections already checked are skipped.
 * </p>
 * <p>
 * Once a state fails to find its connected state, that is, there are no conditional connections that matched its
 * condition, the evaluation will check the presence of a fallback connection on the player state.
 * <ul>
 *     <li>If there is a fallback connection, the current state will move to the connected state, and evaluation will
 *     resume.</li>
 *     <li>If not, the evaluation is concluded, and current state is returned as result.</li>
 * </ul>
 * Trying to create recursion via fallback connection will create a registration error.
 * </p>
 * <p>
 * To register new player states or connections, see {@link MovementPlugin}. To see Paraglider's default set of states
 * and flags, see {@link ParagliderPlayerStates} and {@link ParagliderPlayerStates.Flags}.
 * </p>
 */
public interface PlayerState {
	/**
	 * @return ID of the state
	 */
	@NotNull Identifier id();

	/**
	 * @return Flag of the state
	 * @see Flags
	 */
	@NotNull @Unmodifiable Set<@NotNull Identifier> flags();

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
	default boolean is(@NotNull Identifier id) {
		Objects.requireNonNull(id, "id == null");
		return id().equals(id);
	}

	/**
	 * Check if this state has given flag.
	 *
	 * @param flag Flag
	 * @return Whether this state has given flag
	 */
	default boolean hasFlag(@NotNull Identifier flag) {
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
