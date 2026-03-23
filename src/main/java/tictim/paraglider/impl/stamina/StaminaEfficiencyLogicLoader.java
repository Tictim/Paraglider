package tictim.paraglider.impl.stamina;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@NullMarked
public final class StaminaEfficiencyLogicLoader {
	private StaminaEfficiencyLogicLoader() {}

	public static @Unmodifiable List<StaminaEfficiencyLogic> loadStaminaEfficiencyLogics() {
		return loadStaminaEfficiencyLogics(ParagliderPluginLoader.get().getStaminaPlugins());
	}

	public static @Unmodifiable List<StaminaEfficiencyLogic> loadStaminaEfficiencyLogics(
			@Unmodifiable List<PluginInstance<StaminaPlugin>> plugins
	) {
		List<StaminaEfficiencyLogic> list = new ArrayList<>();

		for (PluginInstance<StaminaPlugin> plugin : plugins) {
			plugin.instance().registerStaminaEfficiencyLogic(new StaminaPlugin.StaminaEfficiencyLogicRegister() {
				@Override public void register(StaminaEfficiencyLogic logic) {
					Objects.requireNonNull(logic, "logic == null");
					list.add(logic);
				}

				@Override public void registerAttribute(
						Supplier<Holder<Attribute>> attribute,
						StaminaPlugin.AttributeEfficiencyCondition condition) {
					Objects.requireNonNull(attribute, "attribute == null");
					Objects.requireNonNull(condition, "condition == null");

					list.add(new AttributeStaminaEfficiencyLogic(attribute, condition));
				}
			});
		}

		return List.copyOf(list);
	}
}
