package tictim.paraglider.impl.stamina;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StaminaEfficiencyLogicLoader {
	private StaminaEfficiencyLogicLoader() {}

	public static @NotNull @Unmodifiable List<StaminaEfficiencyLogic> loadStaminaEfficiencyLogics() {
		return loadStaminaEfficiencyLogics(ParagliderPluginLoader.get().getStaminaPlugins());
	}

	public static @NotNull @Unmodifiable List<StaminaEfficiencyLogic> loadStaminaEfficiencyLogics(
			@NotNull @Unmodifiable List<@NotNull PluginInstance<StaminaPlugin>> plugins
	) {
		List<StaminaEfficiencyLogic> list = new ArrayList<>();

		for (PluginInstance<StaminaPlugin> plugin : plugins) {
			plugin.instance().registerStaminaEfficiencyLogic(logic -> {
				Objects.requireNonNull(logic, "logic == null");
				list.add(logic);
			});
		}

		return List.copyOf(list);
	}
}
