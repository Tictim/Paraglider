package tictim.paraglider.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.plugin.ParagliderPluginBase;
import tictim.paraglider.api.plugin.PluginInstance;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.plugin.ParagliderPluginLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ForgeParagliderPluginLoader implements ParagliderPluginLoader{
	private ForgeParagliderPluginLoader(){}

	private static final ForgeParagliderPluginLoader instance = new ForgeParagliderPluginLoader();

	@NotNull public static ForgeParagliderPluginLoader get(){
		return instance;
	}

	private static final Type annotationType = Type.getType(ParagliderPlugin.class);
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

		for(ModFileScanData scanData : ModList.get().getAllScanData()){
			for(ModFileScanData.AnnotationData annotation : scanData.getAnnotations()){
				if(!annotationType.equals(annotation.annotationType())) continue;
				Class<?> clazz;
				try{
					clazz = Class.forName(
							annotation.clazz().getClassName(),
							true,
							ForgeParagliderPluginLoader.class.getClassLoader());
				}catch(ClassNotFoundException e){
					ParagliderMod.LOGGER.debug("Failed to load plugin {}: ", annotation.clazz().getClassName(), e);
					continue;
				}
				if(!checkPluginType(clazz)){
					ParagliderMod.LOGGER.debug(
							"Plugin {} doesn't implement any of the available plugin types; available plugin types are:\n  {}",
							clazz, Arrays.stream(allPluginTypes)
									.map(c -> c.getCanonicalName())
									.collect(Collectors.joining("\n  ")));
					continue;
				}
				try{
					String modID = getModID(scanData);
					plugins.add(new PluginInstance<>((ParagliderPluginBase)clazz.getConstructor().newInstance(), modID));
					if(modID!=null){
						ParagliderMod.LOGGER.debug("Loaded plugin {} associated with mod {}", clazz, modID);
					}else{
						ParagliderMod.LOGGER.debug("Loaded plugin {}", clazz);
						ParagliderMod.LOGGER.warn("Cannot read mod ID associated to plugin {}", clazz);
					}
				}catch(InstantiationException|InvocationTargetException e){
					ParagliderMod.LOGGER.debug("Failed to load plugin {}: ", clazz, e);
				}catch(IllegalAccessException|NoSuchMethodException e){
					ParagliderMod.LOGGER.debug("Failed to load plugin {}: Constructor not available",
							clazz);
				}
			}
		}

		ParagliderMod.LOGGER.debug("Loaded {} plugins total", plugins.size());

		for(PluginInstance<?> plugin : plugins){
			if(plugin.instance() instanceof StaminaPlugin) staminaPlugins.add(plugin.cast(StaminaPlugin.class));
			if(plugin.instance() instanceof MovementPlugin) movementPlugins.add(plugin.cast(MovementPlugin.class));
		}
	}

	private static boolean checkPluginType(@NotNull Class<?> clazz){
		for(Class<?> c : allPluginTypes){
			if(c.isAssignableFrom(clazz)) return true;
		}
		return false;
	}

	@Nullable private static String getModID(ModFileScanData scanData){
		List<IModFileInfo> data = scanData.getIModInfoData();
		if(data.isEmpty()) return null;
		List<IModInfo> modInfo = data.get(0).getMods();
		if(modInfo.isEmpty()) return null;
		return modInfo.get(0).getModId();
	}
}
