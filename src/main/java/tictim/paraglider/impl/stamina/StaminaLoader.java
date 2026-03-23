package tictim.paraglider.impl.stamina;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.plugin.PluginAction;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaFactory;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.api.stamina.StaminaPluginAction.ProvideStaminaFactory;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.plugin.ParagliderPluginUtils.composePluginLoadingError;
import static tictim.paraglider.plugin.ParagliderPluginUtils.resolve;

@NullMarked
public final class StaminaLoader {
	private StaminaLoader() {}

	// stamina factory with modid that registered the factory
	public static Pair<StaminaFactory, @Nullable String> loadStaminaFactory() {
		return loadStaminaFactory(ParagliderPluginLoader.get().getStaminaPlugins());
	}

	// stamina factory with modid that registered the factory
	private static Pair<StaminaFactory, @Nullable String> loadStaminaFactory(
			List<PluginInstance<StaminaPlugin>> plugins
	) {
		List<PluginAction<StaminaPlugin, ProvideStaminaFactory>> factories = new ArrayList<>();

		for (PluginInstance<StaminaPlugin> plugin : plugins) {
			StaminaFactory factory = plugin.instance().getStaminaFactory();
			if (factory == null) continue;
			factories.add(new PluginAction<>(plugin, new ProvideStaminaFactory(factory)));
		}

		return resolveAndGet(factories, false);
	}

	private static Pair<StaminaFactory, @Nullable String> resolveAndGet(
			List<PluginAction<StaminaPlugin, ProvideStaminaFactory>> factories, boolean resolved) {
		return switch (factories.size()) {
			case 0 -> Pair.of(new BotWStaminaFactory(), ParagliderAPI.MODID);
			case 1 -> Pair.of(factories.get(0).action().factory(), factories.get(0).plugin().modid());
			default -> {
				if (resolved) throw composePluginLoadingError(factories);

				var resolvedActions = resolve(
						StaminaPlugin::getStaminaPluginConflictResolver,
						factories);
				if (resolvedActions == null) throw composePluginLoadingError(factories);
				yield resolveAndGet(resolvedActions, true);
			}
		};
	}

	public static Pair<Boolean, @Nullable String> loadStaminaWheelRemoverId() {
		for (PluginInstance<StaminaPlugin> plugin : ParagliderPluginLoader.get().getStaminaPlugins()) {
			if (plugin.instance().removeStaminaWheel()) {
				return Pair.of(true, plugin.modid());
			}
		}

		return Pair.of(false, null);
	}
}
