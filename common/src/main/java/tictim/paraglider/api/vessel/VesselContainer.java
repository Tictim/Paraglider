package tictim.paraglider.api.vessel;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;

/**
 * <p>
 * Interface providing access to "vessels", a.k.a. Heart Containers and Stamina Vessels. "Essences" can also be accessed
 * with this interface.
 * </p>
 * <p>
 * Note that essences are not synced to clients; only heart containers and stamina vessels are synced.
 * </p>
 */
public interface VesselContainer{
	/**
	 * Get a vessel container instance bound to specific player; if there's none, a no-op singleton implementation is
	 * returned.
	 *
	 * @param player Player
	 * @return A vessel container instance bound to specific player, or a no-op singleton implementation
	 */
	@NotNull static VesselContainer get(@NotNull Player player){
		return ParagliderAPI.vesselContainerSupplier().apply(player);
	}

	/**
	 * @return Number of Heart Containers. Minimum value of {@code 0}.
	 */
	int heartContainer();
	/**
	 * @return Number of Stamina Vessels. Minimum value of {@code 0}.
	 */
	int staminaVessel();
	/**
	 * @return Number of Essences. Minimum value of {@code 0}.
	 */
	int essence();

	/**
	 * Attempts to set the amount of Heart Containers equal to {@code amount}. Depending on
	 *
	 * @param amount     Amount of the Heart Container
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Whether the action succeed
	 */
	@NotNull SetResult setHeartContainer(int amount, boolean simulate, boolean playEffect);

	/**
	 * Attempts to set the amount of Stamina Vessels equal to {@code amount}. Depending on
	 *
	 * @param amount     Amount of the Stamina Vessel
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Whether the action succeed
	 */
	@NotNull SetResult setStaminaVessel(int amount, boolean simulate, boolean playEffect);

	/**
	 * Attempts to set the amount of Essences equal to {@code amount}. Depending on
	 *
	 * @param amount     Amount of the Essence
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Whether the action succeed
	 */
	@NotNull SetResult setEssence(int amount, boolean simulate, boolean playEffect);

	/**
	 * Attempts to give this container Heart Containers to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Heart Containers
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Heart Containers given; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int giveHeartContainers(int amount, boolean simulate, boolean playEffect);
	/**
	 * Attempts to give this container Stamina Vessels to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Stamina Vessels
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Stamina Vessels given; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int giveStaminaVessels(int amount, boolean simulate, boolean playEffect);
	/**
	 * Attempts to give this container Essences to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Essences
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Essences given; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int giveEssences(int amount, boolean simulate, boolean playEffect);

	/**
	 * Attempts to take away Heart Containers from this container to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Heart Containers
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Heart Containers taken; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int takeHeartContainers(int amount, boolean simulate, boolean playEffect);
	/**
	 * Attempts to take away Stamina Vessels from this container to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Stamina Vessels
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Stamina Vessels taken; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int takeStaminaVessels(int amount, boolean simulate, boolean playEffect);
	/**
	 * Attempts to take away Essences from this container to the maximum amount of {@code amount}.
	 *
	 * @param amount     Amount of the Essences
	 * @param simulate   If {@code true}, the result of the action won't be applied to this container; the returned
	 *                   value is what would be expected as a result of subsequent non-simulated action, assuming there
	 *                   wasn't any state modification happened between the simulation and the actual action.
	 * @param playEffect If {@code true}, effects like particles and sound effects may be played. Only applies to
	 *                   successful non-simulation actions.<br> Note that this is not guaranteed; depending on
	 *                   implementation this value might not affect anything.
	 * @return Amount of Essences taken; if {@code amount <= 0}, then the returned value is {@code 0}.
	 */
	int takeEssences(int amount, boolean simulate, boolean playEffect);

	/**
	 * Result of the various setter actions in {@link VesselContainer}.
	 */
	enum SetResult{
		/**
		 * Action completed without hassle.
		 */
		OK,
		/**
		 * Action completed, state of the {@link VesselContainer} remains unchanged.
		 */
		NO_CHANGE,
		/**
		 * The input value is too high.
		 */
		TOO_HIGH,
		/**
		 * The input value is too low.
		 */
		TOO_LOW,
		/**
		 * Failed for other reasons; ex. unsupported operations, internal error etc.
		 */
		FAIL
	}
}
