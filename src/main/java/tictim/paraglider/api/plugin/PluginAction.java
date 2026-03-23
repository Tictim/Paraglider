package tictim.paraglider.api.plugin;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Pair of plugin and action.
 *
 * @param plugin Plugin.
 * @param action Action.
 * @param <P>    Plugin.
 * @param <A>    Action.
 */
@NullMarked
public record PluginAction<P extends ParagliderPluginBase, A>(
		PluginInstance<P> plugin,
		A action
) {
	public PluginAction {
		Objects.requireNonNull(plugin, "plugin == null");
		Objects.requireNonNull(action, "action == null");
	}
}
