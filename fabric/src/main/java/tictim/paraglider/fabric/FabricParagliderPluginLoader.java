package tictim.paraglider.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.plugin.ParagliderPluginBase;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class FabricParagliderPluginLoader implements ParagliderPluginLoader{
	private FabricParagliderPluginLoader(){}

	private static final FabricParagliderPluginLoader instance = new FabricParagliderPluginLoader();

	@NotNull public static FabricParagliderPluginLoader get(){
		return instance;
	}

	private static final Class<?>[] allPluginTypes = {
			StaminaPlugin.class,
			MovementPlugin.class
	};

	private boolean initialized;

	private final List<PluginInstance<StaminaPlugin>> staminaPlugins = new ArrayList<>();
	private final List<PluginInstance<MovementPlugin>> movementPlugins = new ArrayList<>();

	@Override @NotNull @Unmodifiable public List<@NotNull PluginInstance<StaminaPlugin>> getStaminaPlugins(){
		checkAndInitialize();
		return Collections.unmodifiableList(staminaPlugins);
	}
	@Override @NotNull @Unmodifiable public List<@NotNull PluginInstance<MovementPlugin>> getMovementPlugins(){
		checkAndInitialize();
		return Collections.unmodifiableList(movementPlugins);
	}

	private void checkAndInitialize(){
		if(initialized) return;
		initialized = true;

		List<PluginInstance<?>> plugins = new ArrayList<>();

		List<EntrypointContainer<ParagliderPluginBase>> containers = FabricLoader.getInstance()
				.getEntrypointContainers(ParagliderPlugin.FABRIC_ENTRYPOINT, ParagliderPluginBase.class);

		for(EntrypointContainer<ParagliderPluginBase> container : containers){
			if(!checkPluginType(container.getEntrypoint())){
				ParagliderMod.LOGGER.debug(
						"Plugin {} doesn't implement any of the available plugin types; available plugin types are:\n  {}",
						container.getEntrypoint(), Arrays.stream(allPluginTypes)
								.map(c -> c.getCanonicalName())
								.collect(Collectors.joining("\n  ")));
				continue;
			}
			String modID = container.getProvider().getMetadata().getId();
			plugins.add(new PluginInstance<>(container.getEntrypoint(), modID));
			ParagliderMod.LOGGER.debug("Loaded plugin {} associated with mod {}",
					container.getEntrypoint().getClass(), modID);
		}

		ParagliderMod.LOGGER.debug("Loaded {} plugins total", plugins.size());

		for(PluginInstance<?> plugin : plugins){
			if(plugin.instance() instanceof StaminaPlugin) staminaPlugins.add(plugin.cast(StaminaPlugin.class));
			if(plugin.instance() instanceof MovementPlugin) movementPlugins.add(plugin.cast(MovementPlugin.class));
		}
	}

	private static boolean checkPluginType(@NotNull ParagliderPluginBase instance){
		for(Class<?> c : allPluginTypes){
			if(c.isInstance(instance)) return true;
		}
		return false;
	}
}
