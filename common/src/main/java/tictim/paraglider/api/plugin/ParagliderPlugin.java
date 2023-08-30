package tictim.paraglider.api.plugin;

import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.stamina.StaminaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for Paraglider plugins. Can load various type of plugins. All plugins need public no-args
 * constructor in order to be instantiated by Paraglider.<br/>
 * This annotation loads plugins in Forge environment. On Fabric environment, they are loaded with entrypoint system
 * with key {@link ParagliderPlugin#FABRIC_ENTRYPOINT}.
 *
 * @see StaminaPlugin
 * @see MovementPlugin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParagliderPlugin{
	String FABRIC_ENTRYPOINT = "paraglider-plugin";
}
