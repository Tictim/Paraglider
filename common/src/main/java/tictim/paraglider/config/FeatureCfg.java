package tictim.paraglider.config;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;

/**
 * Easy to access switches to toggle side features on and off.<br>
 * Most of them requires server restart or datapack reload. All of them, actually.
 */
public interface FeatureCfg{
	@NotNull static FeatureCfg get(){
		return ParagliderMod.instance().getFeatureConfig();
	}

	/**
	 * For those who wants to remove Spirit Orbs generated in the world, more specifically...
	 * <ul>
	 * <li>Spirit Orbs generated in various chests</li>
	 * <li>Spirit Orbs dropped by spawners and such</li>
	 * </ul>
	 * Note that bargain recipe for Heart Containers/Stamina Vessels will persist, even if this option is disabled.
	 *
	 * @return Config value
	 */
	boolean enableSpiritOrbGens();

	/**
	 * For those who wants to remove entirety of Heart Containers from the game, more specifically...
	 * <ul>
	 * <li>Heart Containers obtained by "challenges" (i.e. Killing dragon, wither, raid)</li>
	 * <li>Bargains using Heart Containers (custom recipes won't be affected)</li>
	 * </ul>
	 * Note that if this option is disabled while staminaVessels is enabled, "challenges" will drop stamina vessels instead.
	 *
	 * @return Config value
	 */
	boolean enableHeartContainers();

	/**
	 * For those who wants to remove entirety of Stamina Vessels from the game, more specifically...
	 * <ul>
	 * <li>Bargains using Stamina Vessels (custom recipes won't be affected)</li>
	 * </ul>
	 *
	 * @return Config value
	 */
	boolean enableStaminaVessels();

	/**
	 * For those who wants to remove all structures added by this mod. Requires restart.
	 *
	 * @return Config value
	 */
	boolean enableStructures();
}
