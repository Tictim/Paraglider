package tictim.paraglider.api.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Pair of plugin instance and optionally associated mod ID.
 *
 * @param instance Plugin instance
 * @param modid    Optional mod ID
 * @param <T>      Type of the plugin
 */
public record PluginInstance<T extends ParagliderPluginBase>(@NotNull T instance, @Nullable String modid){
	public PluginInstance{
		Objects.requireNonNull(instance, "instance == null");
	}

	/**
	 * Casts {@code this} to type of {@code T2}. If the type does not match, an exception is thrown.
	 *
	 * @param clazz Class object of the new type
	 * @param <T2>  New type
	 * @return This
	 * @throws ClassCastException   If the plugin instance cannot be cast to {@code T2}
	 * @throws NullPointerException If {@code clazz == null}
	 */
	@SuppressWarnings("unchecked")
	@NotNull public <T2 extends ParagliderPluginBase> PluginInstance<T2> cast(@NotNull Class<T2> clazz){
		if(!clazz.isInstance(instance)){
			throw new ClassCastException("Cannot cast plugin "+instance+" to "+clazz);
		}
		return (PluginInstance<T2>)this;
	}
}
