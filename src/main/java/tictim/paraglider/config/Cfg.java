package tictim.paraglider.config;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;

/**
 * Instance providing access to config values. This instance is synchronized across server and client; the config
 * values on client side is expected to be identical to
 */
public interface Cfg {
	static @NotNull Cfg get() {
		return ParagliderMod.instance().getConfig();
	}

	/**
	 * Wind sources will generate updrafts that can be used with Paraglider.
	 *
	 * @return Config value
	 */
	boolean updraft();

	/**
	 * Multiplier to horizontal movement speed while paragliding. Value of {@code 0.5} means
	 * 50% of the speed, {@code 2.0} means two times the speed and so forth.
	 *
	 * @return Config value
	 */
	double paraglidingSpeed();

	/**
	 * Durability of paragliders. Set to zero to disable durability.
	 *
	 * @return Config value
	 */
	int paragliderDurability();

	// spirit orbs

	/**
	 * If {@code true}, Ender Dragon will drop heart container(stamina vessel if heart container is disabled) upon
	 * death.
	 *
	 * @return Config value
	 */
	boolean enderDragonDropsVessel();

	/**
	 * If {@code true}, Wither will drop heart container(stamina vessel if heart container is disabled) upon death.
	 *
	 * @return Config value
	 */
	boolean witherDropsVessel();

	/**
	 * If {@code true}, Raids will give heart container(stamina vessel if heart container is disabled) upon victory.<br>
	 * The vessel reward is per-player, meaning every player participated in the Raid will get one vessel each.
	 *
	 * @return Config value
	 */
	boolean raidGivesVessel();

	/**
	 * Amount of Spirit Orbs dropped from spawners.
	 *
	 * @return Config value
	 */
	int spawnerSpiritOrbDrops();

	/**
	 * If true, various types of chest will have chances of having Spirit Orbs inside.<br/>
	 * Does not change contents of already generated chests.
	 *
	 * @return Config value
	 */
	boolean spiritOrbLoots();

	// vessels

	/**
	 * Starting health points measured in number of hearts.
	 *
	 * @return Config value
	 */
	int startingHearts();

	/**
	 * Maximum amount of Heart Containers one player can consume.<br>
	 * Do note that the maximum health point is capped at value of 1024 (or 512 hearts) by Minecraft's default
	 * attribute system; without modifying these limits, Heart Containers won't give you extra hearts beyond that.
	 *
	 * @return Config value
	 */
	int maxHeartContainers();

	/**
	 * Amount of stamina players starts with. One full stamina wheel is equivalent to 1000 stamina.
	 *
	 * @return Config value
	 */
	int startingStamina();

	/**
	 * Maximum amount of Stamina Vessels one player can consume. Higher value = higher maximum stamina.
	 *
	 * @return Config value
	 */
	int maxStaminaVessels();

	/**
	 * Stamina increase per vessel. One full stamina wheel is equivalent to 1000 stamina.
	 *
	 * @return Config value
	 */
	int staminaIncreasePerVessel();

	// stamina

	/**
	 * Paragliding will consume stamina.
	 *
	 * @return Config value
	 */
	boolean paraglidingConsumesStamina();
	/**
	 * Certain non-paragliding actions, such as running and swimming, will consume stamina.
	 *
	 * @return Config value
	 */
	boolean runningConsumesStamina();

	// compat

	/**
	 * Configurable option for Towers of the Wild compat feature. Can be ignored if Towers of the Wild is not
	 * installed.<br>
	 * {@code DEFAULT}: Default option, spawn Deku Leaf in ocean tower chests and Paraglider in normal tower chests<br>
	 * {@code DISABLE}: Don't spawn anything<br>
	 * {@code PARAGLIDER_ONLY}: Spawn paraglider in both ocean and normal tower chests<br>
	 * {@code DEKU_LEAF_ONLY}: Spawn deku leaf in both ocean and normal tower chests, like a boss<br>
	 *
	 * @return Config value
	 */
	@NotNull TotwCompatConfigOption paragliderInTowersOfTheWild();

	default int additionalMaxHealth(int heartContainers) {
		return (startingHearts() - 10 + Math.min(maxHeartContainers(), heartContainers)) * 2;
	}

	default int maxStamina(int staminaVessels) {
		return Math.max(0, startingStamina() + Math.max(0, Math.min(staminaVessels, maxStaminaVessels())) * staminaIncreasePerVessel());
	}

	enum TotwCompatConfigOption {
		/**
		 * Default option, spawn Deku Leaf in ocean tower chests and Paraglider in normal tower chests
		 */
		DEFAULT,
		/**
		 * Don't spawn anything
		 */
		DISABLE,
		/**
		 * Spawn paraglider in both ocean and normal tower chests
		 */
		PARAGLIDER_ONLY,
		/**
		 * Spawn deku leaf in both ocean and normal tower chests, like a boss
		 */
		DEKU_LEAF_ONLY
	}
}
