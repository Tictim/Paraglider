package tictim.paraglider.api.movement;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.plugin.ParagliderPluginBase;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Plugin for movement-related features. Refer to {@link PlayerState} for explanations on player state and connections.
 *
 * @see ParagliderPlugin
 */
@NullMarked
public interface MovementPlugin extends ParagliderPluginBase {
	default void registerNewStates(PlayerStateRegister register) {}

	default void modifyRegisteredStates(PlayerStateModifier modifier) {}

	default void registerStateConnections(PlayerStateConnectionRegister register) {}

	/**
	 * @return Implementation of {@link ConflictResolver} for this {@link MovementPlugin} instance
	 */
	default ConflictResolver<MovementPlugin, MovementPluginAction> getMovementPluginConflictResolver() {
		return ConflictResolver.proceed();
	}

	/**
	 * Interface for registering player states to the player movement system.
	 */
	interface PlayerStateRegister {
		/**
		 * Register a state. If another state has been registered with same ID, it will create a conflict; see
		 * {@link ConflictResolver}.
		 *
		 * @param id                  ID of the new state
		 * @param defaultStaminaDelta Stamina delta of the state; positive values represent a state which
		 *                            replenishes stamina. Negative values represent a state which drains stamina.
		 *                            0 represents a neutral state. Note this value is only for providing default
		 *                            values; the final value used by the game can be changed with configs.
		 * @param flags               Flags of the state
		 * @throws NullPointerException     If {@code id == null}, {@code defaultFlags == null}, or any element of
		 *                                  {@code flags} is null
		 * @throws IllegalArgumentException If {@code Double.isNaN(defaultStaminaDelta) == true}
		 */
		void register(Identifier id, double defaultStaminaDelta, Identifier... flags);

		/**
		 * Register a synthetic state. If another state has been registered with same ID, it will create a conflict;
		 * see {@link ConflictResolver}.
		 * <p/>
		 * Synthetic states are purely used for creating connection with other states. Fallback connections are required
		 * for synthetic states. Not providing the fallback connection will result in an error. To register fallback
		 * connections, see {@link PlayerStateConnectionRegister#setFallback(Identifier, Identifier)}.
		 *
		 * @param id ID of the new state
		 * @throws NullPointerException If {@code id == null}
		 */
		void registerSyntheticState(Identifier id);
	}

	/**
	 * Interface for modifying properties of registered player states.
	 */
	interface PlayerStateModifier {
		/**
		 * Get all registered player states, mapped to its ID. The collection is immutable, and attempting to modify the
		 * collection will throw an exception. Note that changes made to the fields of the player states, such as
		 * stamina delta or flags, will not be reflected to the fields of corresponding instance; player states with
		 * finalized attributes will be available in {@link #registerStateConnections(PlayerStateConnectionRegister)
		 * registerStateConnections} stage.
		 *
		 * @return Map of registered player states
		 */
		@Unmodifiable Map<Identifier, PlayerState> playerStates();

		/**
		 * See if a state with ID {@code id} is registered.
		 *
		 * @param id ID of the state
		 * @return Whether a state is registered with ID {@code id}
		 * @throws NullPointerException If {@code id == null}
		 */
		default boolean exists(Identifier id) {
			return playerStates().containsKey(Objects.requireNonNull(id, "id == null"));
		}

		/**
		 * Change a state's default stamina delta. Positive values represent a state which replenishes stamina.
		 * Negative values represent a state which drains stamina. 0 represents a neutral state. Note this value is only
		 * for providing default values; the final value used by the game can be changed with configs.
		 * <p/>
		 * Trying to use this method against synthetic states will result in error. If two different values are supplied
		 * for one state, it will create a conflict; see {@link ConflictResolver}.
		 *
		 * @param id                  ID of the state
		 * @param defaultStaminaDelta New value for stamina data
		 * @throws NullPointerException     If {@code id == null}
		 * @throws NoSuchElementException   If there's no state with ID {@code id}
		 * @throws IllegalStateException    If the state is synthetic state
		 * @throws IllegalArgumentException If {@code Double.isNaN(defaultStaminaDelta) == true}
		 */
		void changeDefaultStaminaDelta(Identifier id, double defaultStaminaDelta);

		/**
		 * Add flags to the state. Trying to use this method against synthetic states will result in error. If the
		 * flags are also marked for removal via {@link #removeFlags(Identifier, Identifier...)}, addition
		 * takes precedence.
		 *
		 * @param id    ID of the state
		 * @param flags Additional flags
		 * @throws NullPointerException   If {@code id == null}, {@code defaultFlags == null}, or any element of {@code
		 *                                defaultFlags} is null
		 * @throws NoSuchElementException If there's no state with ID {@code id}
		 * @throws IllegalStateException  If the state is synthetic state
		 */
		void addFlags(Identifier id, Identifier... flags);

		/**
		 * Remove flags from the state. Trying to use this method against synthetic states will result in error. If the
		 * flags are also marked for addition via {@link #addFlags(Identifier, Identifier...)}, addition
		 * takes precedence.
		 *
		 * @param id    ID of the state
		 * @param flags Flags to remove
		 * @throws NullPointerException   If {@code id == null}, {@code defaultFlags == null}, or any element of {@code
		 *                                defaultFlags} is null
		 * @throws NoSuchElementException If there's no state with ID {@code id}
		 * @throws IllegalStateException  If the state is synthetic state
		 */
		void removeFlags(Identifier id, Identifier... flags);
	}

	/**
	 * Interface for registering connections between player states.
	 */
	interface PlayerStateConnectionRegister {
		/**
		 * Get all registered player states, mapped to its ID. The collection is immutable, and attempting to modify the
		 * collection will throw an exception.
		 *
		 * @return Map of registered player states
		 */
		@Unmodifiable Map<Identifier, PlayerState> playerStates();

		/**
		 * See if a state with ID {@code id} is registered.
		 *
		 * @param id ID of the state
		 * @return Whether a state is registered with ID {@code id}
		 * @throws NullPointerException If {@code id == null}
		 */
		default boolean exists(Identifier id) {
			return playerStates().containsKey(Objects.requireNonNull(id, "id == null"));
		}

		/**
		 * Add a connection from {@code parent} to {@code state} with given condition and priority of {@code 0}.
		 * If {@code state} is equal to {@code parent}, the connection will do nothing.
		 *
		 * @param parent    ID of the parent state
		 * @param condition Condition of the connection
		 * @param state     ID of the state
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 * @see #connect(Identifier, Identifier, PlayerStateCondition, double)
		 */
		default void connect(Identifier parent, Identifier state, PlayerStateCondition condition) {
			connect(parent, state, condition, 0);
		}

		/**
		 * Add a connection from {@code parent} to {@code state} with given condition and priority. If
		 * {@code state} is equal to {@code parent}, the connection will do nothing.
		 *
		 * @param parent    ID of the parent state
		 * @param condition Condition of the connection
		 * @param state     ID of the state
		 * @param priority  Priority of the condition; connection with higher priority has precedence over other
		 *                  connections with lower priorities. If two connections share same priority, they will be
		 *                  evaluated on registration order.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 */
		void connect(Identifier parent, Identifier state, PlayerStateCondition condition, double priority);

		/**
		 * Remove all <i>conditioned</i> connections that match given property. This method takes precedence over
		 * additions via {@link #connect(Identifier, Identifier, PlayerStateCondition, double)}.
		 *
		 * @param parent   ID of the parent state
		 * @param state    ID of the state
		 * @param priority Priority of the condition. If {@code null} is specified, all connection from {@code parent}
		 *                 to {@code state} will be removed.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 */
		void disconnect(Identifier parent, Identifier state, @Nullable Double priority);

		/**
		 * Set a fallback connection from {@code parent} to {@code fallback}, or remove preexisting fallback connection
		 * if {@code null} is given for the {@code fallback} parameter. If a fallback connection is present, when all
		 * the conditioned connections for {@code parent} state are failed to match, evaluation will jump to
		 * {@code fallback} state and continue from there instead of terminating and outputting the
		 * {@code parent} state. Trying to set itself as fallback state will produce an exception.
		 * <p/>
		 * Contrary to conditioned connections, fallback connections cannot form circular dependencies. Attempting to
		 * create circular dependency will result in an error.
		 * <p/>
		 * If two fallback connections have same parent, same priority, and different target, then it will create a
		 * conflict; see {@link ConflictResolver}.
		 *
		 * @param parent   ID of the parent state
		 * @param fallback ID of the fallback state
		 * @throws NullPointerException     If any of the parameters is {@code null}
		 * @throws NoSuchElementException   If there's no state with ID {@code parent} or {@code fallback}
		 * @throws IllegalArgumentException If {@code parent == fallback}
		 * @see #setFallback(Identifier, Identifier, double)
		 */
		default void setFallback(Identifier parent, @Nullable Identifier fallback) {
			setFallback(parent, fallback, 0);
		}

		/**
		 * Set a fallback connection from {@code parent} to {@code fallback}, or remove preexisting fallback connection
		 * if {@code null} is given for the {@code fallback} parameter. If a fallback connection is present, when all
		 * the conditioned connections for {@code parent} state are failed to match, evaluation will jump to
		 * {@code fallback} state and continue from there instead of terminating and outputting the
		 * {@code parent} state. Trying to set itself as fallback state will produce an exception.
		 * <p/>
		 * Contrary to conditioned connections, fallback connections cannot form circular dependencies. Attempting to
		 * create circular dependency will result in an error.
		 * <p/>
		 * If two fallback connections have same parent, same priority and different target, then it will create a
		 * conflict; see {@link ConflictResolver}.
		 *
		 * @param parent   ID of the parent state
		 * @param fallback ID of the fallback state
		 * @param priority Priority of this fallback connection; only the fallback connection with the <b>highest</b>
		 *                 priority will be applied.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code fallback}
		 */
		void setFallback(Identifier parent, @Nullable Identifier fallback, double priority);
	}
}
