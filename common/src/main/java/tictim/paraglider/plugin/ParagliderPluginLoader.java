package tictim.paraglider.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.util.List;

public interface ParagliderPluginLoader{
	@NotNull static ParagliderPluginLoader get(){
		return ParagliderMod.instance().getPluginLoader();
	}

	@NotNull @Unmodifiable List<@NotNull PluginInstance<StaminaPlugin>> getStaminaPlugins();
	@NotNull @Unmodifiable List<@NotNull PluginInstance<MovementPlugin>> getMovementPlugins();
}
