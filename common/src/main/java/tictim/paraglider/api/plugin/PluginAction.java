package tictim.paraglider.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Pair of plugin and action.
 *
 * @param plugin Plugin.
 * @param action Action.
 * @param <P>    Plugin.
 * @param <A>    Action.
 */
public record PluginAction<P extends ParagliderPluginBase, A>(
		@NotNull PluginInstance<P> plugin,
		@NotNull A action
){
	public PluginAction{
		Objects.requireNonNull(plugin, "plugin == null");
		Objects.requireNonNull(action, "action == null");
	}
}
