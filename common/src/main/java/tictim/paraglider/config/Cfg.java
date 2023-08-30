package tictim.paraglider.config;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;

/**
 * Instance providing access to config values. This instance is synchronized across server and client; the config
 * values on client side is expected to be identical to
 */
public interface Cfg{
	@NotNull static Cfg get(){
		return ParagliderMod.instance().getConfig();
	}

	/**
	 * Fire will float you upwards.
	 *
	 * @return Config value
	 */
	boolean ascendingWinds();

	/**
	 * You can customize which block produces wind.<br/>
	 * Write each blockstate to one of this format:
	 * <pre>
	 *   [block ID]   (Matches all state of the block)
	 *   [block ID]#[property1=value],[property2=value],[property3=value]   (Matches state of the block that has specified properties)
	 *   #[Tag ID]   (Matches all blocks with the tag)
	 * </pre>
	 * Same property cannot be specified multiple times. Wind sources with any invalid part will be excluded.
	 *
	 * @return Instance of {@link BlockMatcher} parsed from config
	 */
	@NotNull BlockMatcher windSourceMatcher();

	/**
	 * Multiplier to horizontal movement speed while paragliding. Value of {@code 0.5} means
	 * 50% of the speed, {@code 2.0} means two times the speed and so forth.
	 *
	 * @return Config value
	 */
	double paraglidingSpeed();

	/**
	 * Durability of Paragliders. Set to zero to disable durability.
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
	 * Maximum amount of stamina Player can get. One third of this value is equal to one stamina wheel.
	 *
	 * @return Config value
	 */
	int maxStamina();

	/**
	 * Amount of stamina Player starts with. Values higher than maxStamina doesn't work.<br>
	 * If you want to make starting stamina displayed as one full stamina wheel, this value should be one
	 * third of maxStamina.
	 *
	 * @return Config value
	 */
	int startingStamina();

	/**
	 * Stamina Vessels players need to obtain max out stamina. More vessels means lesser stamina increase per vessel.
	 *
	 * @return Config value
	 */
	int maxStaminaVessels();

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

	default boolean isWindSource(@NotNull BlockState state){
		return windSourceMatcher().test(state);
	}

	default int additionalMaxHealth(int heartContainers){
		return (startingHearts()-10+Math.min(maxHeartContainers(), heartContainers))*2;
	}

	default int maxStamina(int staminaVessels){
		int maxStaminaVessels = maxStaminaVessels();
		int startingStamina = startingStamina();
		if(maxStaminaVessels<=0) return startingStamina;
		if(maxStaminaVessels<=staminaVessels) maxStamina();
		return startingStamina+(int)((double)staminaVessels/maxStaminaVessels*(maxStamina()-startingStamina));
	}

	enum TotwCompatConfigOption{
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
