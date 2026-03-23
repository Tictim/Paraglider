package tictim.paraglider.plugin;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.util.List;

@NullMarked
public interface ParagliderPluginLoader {
	static ParagliderPluginLoader get() {
		return ParagliderMod.instance().getPluginLoader();
	}

	@Unmodifiable List<PluginInstance<StaminaPlugin>> getStaminaPlugins();
	@Unmodifiable List<PluginInstance<MovementPlugin>> getMovementPlugins();
}
