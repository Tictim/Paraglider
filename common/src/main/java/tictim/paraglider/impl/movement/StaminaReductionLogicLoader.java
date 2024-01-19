package tictim.paraglider.impl.movement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.movement.StaminaReductionLogic;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StaminaReductionLogicLoader{
	private StaminaReductionLogicLoader(){}

	@NotNull @Unmodifiable public static List<StaminaReductionLogic> loadStaminaReductionLogics(){
		return loadStaminaReductionLogics(ParagliderPluginLoader.get().getMovementPlugins());
	}

	@NotNull @Unmodifiable public static List<StaminaReductionLogic> loadStaminaReductionLogics(
			@NotNull List<@NotNull PluginInstance<MovementPlugin>> plugins
	){
		List<StaminaReductionLogic> list = new ArrayList<>();

		for(PluginInstance<MovementPlugin> plugin : plugins){
			plugin.instance().registerStaminaReductionLogic(logic -> {
				Objects.requireNonNull(logic, "logic == null");
				list.add(logic);
			});
		}

		return List.copyOf(list);
	}
}
