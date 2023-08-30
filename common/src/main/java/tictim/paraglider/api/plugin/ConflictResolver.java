package tictim.paraglider.api.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for resolving conflicting actions between one of more plugins.
 * <p/>
 * The conflict usually occurs when two or more plugins attempt to apply changes that are mutually exclusive to each
 * other. When a conflicting actions are detected, before hard crashing, the game will use conflict resolver instance
 * associated to each plugin to determine which action should be taken.
 *
 * @param <P> Type of the plugin
 * @param <A> Type of the action
 * @see #resolveConflict(Object, PluginAction[])
 */
@FunctionalInterface
public interface ConflictResolver<P extends ParagliderPluginBase, A>{
	/**
	 * @param <P> Type of the plugin
	 * @param <A> Type of the action
	 * @return Implementations of {@link ConflictResolver} that proceeds with all conflicts.
	 */
	@SuppressWarnings("unchecked")
	@NotNull static <P extends ParagliderPluginBase, A> ConflictResolver<P, A> proceed(){
		return (ConflictResolver<P, A>)Internal.ALWAYS_PROCEED;
	}
	/**
	 * @param <P> Type of the plugin
	 * @param <A> Type of the action
	 * @return Implementations of {@link ConflictResolver} that aborts all conflicts.
	 */
	@SuppressWarnings("unchecked")
	@NotNull static <P extends ParagliderPluginBase, A> ConflictResolver<P, A> abort(){
		return (ConflictResolver<P, A>)Internal.ALWAYS_ABORT;
	}
	/**
	 * @param <P> Type of the plugin
	 * @param <A> Type of the action
	 * @return Implementations of {@link ConflictResolver} that produces error with all conflicts.
	 */
	@SuppressWarnings("unchecked")
	@NotNull static <P extends ParagliderPluginBase, A> ConflictResolver<P, A> error(){
		return (ConflictResolver<P, A>)Internal.ALWAYS_ERROR;
	}

	/**
	 * Attempt to resolve conflict between this and another plugins. The conflict usually occurs when two or more
	 * plugins attempt to apply changes that are mutually exclusive to each other.
	 * <p/>
	 * This method is called for each conflicting plugin. The return value specifies action to be taken against
	 * previously made change; see {@link Resolution} for more info.
	 *
	 * @param action             Instance of the conflicting action made by this plugin
	 * @param conflictingPlugins List of plugins conflicting with this plugin
	 * @return Action to be taken against previously made change
	 * @see Resolution
	 */
	@NotNull Resolution resolveConflict(@NotNull A action, @NotNull PluginAction<? extends P, ? extends A> @NotNull [] conflictingPlugins);

	/**
	 * Specifies the action to be taken against previously made change.
	 *
	 * @see Resolution#PROCEED
	 * @see Resolution#ABORT
	 * @see Resolution#ERROR
	 */
	enum Resolution{
		/**
		 * Proceed with the change. If two or more plugins decide to proceed, it will halt the loading process and print
		 * out the cause.
		 */
		PROCEED,
		/**
		 * Abort the previously made change.
		 */
		ABORT,
		/**
		 * Halt the loading process and print out an error. This action will take place regardless of other plugin's
		 * resolutions.
		 */
		ERROR
	}
}

class Internal{
	static final ConflictResolver<?, ?> ALWAYS_PROCEED = (a, p) -> ConflictResolver.Resolution.PROCEED;
	static final ConflictResolver<?, ?> ALWAYS_ABORT = (a, p) -> ConflictResolver.Resolution.ABORT;
	static final ConflictResolver<?, ?> ALWAYS_ERROR = (a, p) -> ConflictResolver.Resolution.ERROR;
}
