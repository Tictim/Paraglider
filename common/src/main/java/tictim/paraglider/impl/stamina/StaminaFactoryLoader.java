package tictim.paraglider.impl.stamina;

import org.jetbrains.annotations.NotNull;
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

public final class StaminaFactoryLoader{
	private StaminaFactoryLoader(){}

	@NotNull public static StaminaFactory loadStaminaFactory(){
		return loadStaminaFactory(ParagliderPluginLoader.get().getStaminaPlugins());
	}

	@NotNull private static StaminaFactory loadStaminaFactory(
			@NotNull List<@NotNull PluginInstance<StaminaPlugin>> plugins
	){
		List<PluginAction<StaminaPlugin, ProvideStaminaFactory>> factories = new ArrayList<>();

		for(PluginInstance<StaminaPlugin> plugin : plugins){
			StaminaFactory factory = plugin.instance().getStaminaFactory();
			if(factory==null) continue;
			factories.add(new PluginAction<>(plugin, new ProvideStaminaFactory(factory)));
		}

		return switch(factories.size()){
			case 0 -> new BotWStaminaFactory();
			case 1 -> factories.get(0).action().factory();
			default -> {
				var resolved = resolve(
						StaminaPlugin::getStaminaPluginConflictResolver,
						factories);
				if(resolved==null) throw composePluginLoadingError(factories);
				yield switch(resolved.size()){
					case 0 -> new BotWStaminaFactory();
					case 1 -> resolved.get(0).action().factory();
					default -> throw composePluginLoadingError(factories);
				};
			}
		};
	}
}
