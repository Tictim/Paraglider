package tictim.paraglider.api.movement;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.plugin.ParagliderPluginBase;

import java.util.NoSuchElementException;

/**
 * Plugin for movement-related features.
 *
 * @see ParagliderPlugin
 */
public interface MovementPlugin extends ParagliderPluginBase{
	default void registerNewStates(@NotNull PlayerStateRegister register){}

	default void modifyRegisteredStates(@NotNull PlayerStateModifier modifier){}

	default void registerStateConnections(@NotNull PlayerStateConnectionRegister register){}

	/**
	 * @return Implementation of {@link ConflictResolver} for this {@link MovementPlugin} instance
	 */
	@NotNull default ConflictResolver<MovementPlugin, MovementPluginAction> getMovementPluginConflictResolver(){
		return ConflictResolver.proceed();
	}

	/**
	 * Interface for registering player states to the player movement system.
	 */
	interface PlayerStateRegister{
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
		 * @throws NullPointerException If {@code id == null}, {@code defaultFlags == null}, or any element of {@code
		 *                              flags} is null
		 */
		void register(@NotNull ResourceLocation id, int defaultStaminaDelta, @NotNull ResourceLocation @NotNull ... flags);

		/**
		 * Register a synthetic state. If another state has been registered with same ID, it will create a conflict;
		 * see {@link ConflictResolver}.
		 * <p/>
		 * Synthetic states are purely used for creating connection with other states. Fallback branches are required
		 * for synthetic states. Not providing the fallback branch will result in an error. To register fallback
		 * branches, see {@link PlayerStateConnectionRegister#setFallback(ResourceLocation, ResourceLocation)}.
		 *
		 * @param id ID of the new state
		 * @throws NullPointerException If {@code id == null}
		 */
		void registerSyntheticState(@NotNull ResourceLocation id);
	}

	/**
	 * Interface for modifying properties of registered player states.
	 */
	interface PlayerStateModifier{
		/**
		 * See if a state with ID {@code id} is registered.
		 *
		 * @param id ID of the state
		 * @return Whether a state is registered with ID {@code id}
		 * @throws NullPointerException If {@code id == null}
		 */
		boolean exists(@NotNull ResourceLocation id);

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
		 * @throws NullPointerException   If {@code id == null}
		 * @throws NoSuchElementException If there's no state with ID {@code id}
		 * @throws IllegalStateException  If the state is synthetic state
		 */
		void changeDefaultStaminaDelta(@NotNull ResourceLocation id, int defaultStaminaDelta);

		/**
		 * Add flags to the state. Trying to use this method against synthetic states will result in error. If the
		 * flags are also marked for removal via {@link #removeFlags(ResourceLocation, ResourceLocation...)}, addition
		 * takes precedence.
		 *
		 * @param id    ID of the state
		 * @param flags Additional flags
		 * @throws NullPointerException   If {@code id == null}, {@code defaultFlags == null}, or any element of {@code
		 *                                defaultFlags} is null
		 * @throws NoSuchElementException If there's no state with ID {@code id}
		 * @throws IllegalStateException  If the state is synthetic state
		 */
		void addFlags(@NotNull ResourceLocation id, @NotNull ResourceLocation @NotNull ... flags);

		/**
		 * Remove flags from the state. Trying to use this method against synthetic states will result in error. If the
		 * flags are also marked for addition via {@link #addFlags(ResourceLocation, ResourceLocation...)}, addition
		 * takes precedence.
		 *
		 * @param id    ID of the state
		 * @param flags Flags to remove
		 * @throws NullPointerException   If {@code id == null}, {@code defaultFlags == null}, or any element of {@code
		 *                                defaultFlags} is null
		 * @throws NoSuchElementException If there's no state with ID {@code id}
		 * @throws IllegalStateException  If the state is synthetic state
		 */
		void removeFlags(@NotNull ResourceLocation id, @NotNull ResourceLocation @NotNull ... flags);
	}

	/**
	 * Interface for registering connections between player states.
	 */
	interface PlayerStateConnectionRegister{
		/**
		 * See if a state with ID {@code id} is registered.
		 *
		 * @param id ID of the state
		 * @return Whether a state is registered with ID {@code id}
		 * @throws NullPointerException If {@code id == null}
		 */
		boolean exists(@NotNull ResourceLocation id);

		/**
		 * Add a conditioned branch from {@code parent} to {@code state} with given condition and priority of {@code 0}.
		 * If {@code state} is equal to {@code parent}, the branch will do nothing.
		 *
		 * @param parent    ID of the parent state
		 * @param condition Condition of the branch
		 * @param state     ID of the state
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 * @see #addBranch(ResourceLocation, PlayerStateCondition, ResourceLocation, double)
		 */
		default void addBranch(@NotNull ResourceLocation parent, @NotNull PlayerStateCondition condition, @NotNull ResourceLocation state){
			addBranch(parent, condition, state, 0);
		}

		/**
		 * Add a conditioned branch from {@code parent} to {@code state} with given condition and priority. If
		 * {@code state} is equal to {@code parent}, the branch will do nothing.
		 *
		 * @param parent    ID of the parent state
		 * @param condition Condition of the branch
		 * @param state     ID of the state
		 * @param priority  Priority of the condition; connection with higher priority has precedence over other
		 *                  connections with lower priorities. If two branches share same priority, they will be
		 *                  evaluated on registration order.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 */
		void addBranch(@NotNull ResourceLocation parent, @NotNull PlayerStateCondition condition, @NotNull ResourceLocation state, double priority);

		/**
		 * Remove all conditioned branches that matches given property. This method takes precedence over additions via
		 * {@link #addBranch(ResourceLocation, PlayerStateCondition, ResourceLocation, double)}.
		 *
		 * @param parent   ID of the parent state
		 * @param state    ID of the state
		 * @param priority Priority of the condition. If {@code null} is specified, all connection from {@code parent}
		 *                 to {@code state} will be removed.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code state}
		 */
		void removeBranch(@NotNull ResourceLocation parent, @NotNull ResourceLocation state, @Nullable Double priority);

		/**
		 * Set a fallback branch from {@code parent} to {@code fallback}, or remove preexisting fallback branch if
		 * {@code null} is given for the {@code fallback} parameter. If a fallback branch is present, when all
		 * the conditioned branches for {@code parent} state are failed to match, evaluation will jump to
		 * {@code fallback} state and continue from there instead of terminating and outputting the
		 * {@code parent} state. Trying to set itself as fallback state will produce an exception.
		 * <p/>
		 * Contrary to conditioned branches, fallback branches cannot form circular dependencies. Attempting to create
		 * circular dependency will result in an error.
		 * <p/>
		 * If two fallback branches have same parent, same priority, and different target, then it will create a
		 * conflict; see {@link ConflictResolver}.
		 *
		 * @param parent   ID of the parent state
		 * @param fallback ID of the fallback state
		 * @throws NullPointerException     If any of the parameters is {@code null}
		 * @throws NoSuchElementException   If there's no state with ID {@code parent} or {@code fallback}
		 * @throws IllegalArgumentException If {@code parent == fallback}
		 * @see #setFallback(ResourceLocation, ResourceLocation, double)
		 */
		default void setFallback(@NotNull ResourceLocation parent, @Nullable ResourceLocation fallback){
			setFallback(parent, fallback, 0);
		}

		/**
		 * Set a fallback branch from {@code parent} to {@code fallback}, or remove preexisting fallback branch if
		 * {@code null} is given for the {@code fallback} parameter. If a fallback branch is present, when all
		 * the conditioned branches for {@code parent} state are failed to match, evaluation will jump to
		 * {@code fallback} state and continue from there instead of terminating and outputting the
		 * {@code parent} state. Trying to set itself as fallback state will produce an exception.
		 * <p/>
		 * Contrary to conditioned branches, fallback branches cannot form circular dependencies. Attempting to create
		 * circular dependency will result in an error.
		 * <p/>
		 * If two fallback branches have same parent, same priority and different target, then it will create a
		 * conflict; see {@link ConflictResolver}.
		 *
		 * @param parent   ID of the parent state
		 * @param fallback ID of the fallback state
		 * @param priority Priority of this fallback branch; only the fallback branch with the <b>highest</b> priority
		 *                 will be applied.
		 * @throws NullPointerException   If any of the parameters is {@code null}
		 * @throws NoSuchElementException If there's no state with ID {@code parent} or {@code fallback}
		 */
		void setFallback(@NotNull ResourceLocation parent, @Nullable ResourceLocation fallback, double priority);
	}
}
