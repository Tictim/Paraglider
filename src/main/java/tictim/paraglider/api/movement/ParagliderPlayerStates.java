package tictim.paraglider.api.movement;

import net.minecraft.resources.ResourceLocation;
import tictim.paraglider.api.ParagliderAPI;

/**
 * Default set of player states provided by Paragliders mod.
 */
public interface ParagliderPlayerStates {
	/**
	 * Default value for {@link PlayerState#recoveryDelay()}.
	 */
	int RECOVERY_DELAY = 10;

	/**
	 * Idle state (just standing). This is considered a default state, and also an entrypoint for state connections.
	 */
	ResourceLocation IDLE = ParagliderAPI.id("idle");
	int IDLE_STAMINA_DELTA = 20;

	/**
	 * Creative mode flying.
	 */
	ResourceLocation FLYING = ParagliderAPI.id("flying");
	int FLYING_STAMINA_DELTA = IDLE_STAMINA_DELTA;
	double FLYING_PRIORITY = 8;

	/**
	 * Creative mode flying, but actually creative.
	 */
	ResourceLocation CREATIVE_FLYING = ParagliderAPI.id("creative_flying");
	int CREATIVE_FLYING_STAMINA_DELTA = IDLE_STAMINA_DELTA;

	/**
	 * Elytra flying.
	 */
	ResourceLocation ELYTRA_FLYING = ParagliderAPI.id("elytra_flying");
	int ELYTRA_FLYING_STAMINA_DELTA = 0;
	double ELYTRA_FLYING_PRIORITY = 7;

	/**
	 * Riding a vehicle.
	 */
	ResourceLocation ON_VEHICLE = ParagliderAPI.id("on_vehicle");
	int ON_VEHICLE_STAMINA_DELTA = IDLE_STAMINA_DELTA;
	double ON_VEHICLE_PRIORITY = 6;

	/**
	 * Swimming.
	 */
	ResourceLocation SWIMMING = ParagliderAPI.id("swimming");
	int SWIMMING_STAMINA_DELTA = -6;
	double SWIMMING_PRIORITY = 5;

	/**
	 * "Underwater" - which actually means just touching water.
	 */
	ResourceLocation UNDERWATER = ParagliderAPI.id("underwater");
	int UNDERWATER_STAMINA_DELTA = 3;
	double UNDERWATER_PRIORITY = 4;

	/**
	 * {@link #UNDERWATER} with one of the "underwater breathing" conditions met:
	 * <ul>
	 *     <li>having potion effect {@code water_breathing};</li>
	 *     <li>standing underwater while breathing, either with bubbles, or other external breathing capability;</li>
	 *     <li>wearing turtle helmet;</li>
	 *     <li>wearing headgear enchanted with {@code aqua_affinity};</li>
	 *     <li>or wearing boots enchanted with {@code depth_strider}.</li>
	 * </ul>
	 */
	ResourceLocation BREATHING_UNDERWATER = ParagliderAPI.id("breathing_underwater");
	int BREATHING_UNDERWATER_STAMINA_DELTA = IDLE_STAMINA_DELTA;

	/**
	 * Paragliding. Duh.
	 */
	ResourceLocation PARAGLIDING = ParagliderAPI.id("paragliding");
	int PARAGLIDING_STAMINA_DELTA = -3;
	double PARAGLIDING_PRIORITY = 3;

	/**
	 * Paragliding without any stamina left.
	 */
	ResourceLocation PANIC_PARAGLIDING = ParagliderAPI.id("panic_paragliding");
	int PANIC_PARAGLIDING_STAMINA_DELTA = -3;

	/**
	 * Paragliding over updraft wind.
	 */
	ResourceLocation ASCENDING = ParagliderAPI.id("ascending");
	int ASCENDING_STAMINA_DELTA = PARAGLIDING_STAMINA_DELTA;

	/**
	 * Running.
	 */
	ResourceLocation RUNNING = ParagliderAPI.id("running");
	int RUNNING_STAMINA_DELTA = -10;
	double RUNNING_PRIORITY = 2;

	/**
	 * Airborne.
	 */
	ResourceLocation MIDAIR = ParagliderAPI.id("midair");
	int MIDAIR_STAMINA_DELTA = 0;
	double MIDAIR_PRIORITY = 1;

	/**
	 * Default set of player state flags used by Paraglider mod.
	 */
	interface Flags {
		/**
		 * If a player is on a state marked by this flag, applies paragliding movement logic to the player. Used by
		 * {@link ParagliderPlayerStates#PARAGLIDING}, {@link ParagliderPlayerStates#PANIC_PARAGLIDING}, and
		 * {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation PARAGLIDING = ParagliderPlayerStates.PARAGLIDING;

		/**
		 * If a player is on a state marked by this flag and {@link Flags#PARAGLIDING}, it will slowly move the
		 * player upwards. Used by {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation ASCENDING = ParagliderPlayerStates.ASCENDING;

		/**
		 * If the config {@code paraglidingConsumesStamina} is set to {@code false}, negative stamina
		 * delta will be considered as zero. Used by {@link ParagliderPlayerStates#PARAGLIDING},
		 * {@link ParagliderPlayerStates#PANIC_PARAGLIDING}, and {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation DISABLED_BY_PARAGLIDING_CONFIG = ParagliderAPI.id("disabled_by_paragliding_config");

		/**
		 * If the config {@code runningConsumesStamina} is set to {@code false}, and the state has negative stamina
		 * delta, it will be considered as zero. Used by {@link ParagliderPlayerStates#SWIMMING},
		 * {@link ParagliderPlayerStates#UNDERWATER}, {@link ParagliderPlayerStates#BREATHING_UNDERWATER}, and
		 * {@link ParagliderPlayerStates#RUNNING}.
		 */
		ResourceLocation DISABLED_BY_RUNNING_CONFIG = ParagliderAPI.id("disabled_by_running_config");

		/**
		 * No inherent functionality. States with this flag are eligible for stamina efficiency effect applied by
		 * {@code paraglider:paragliding_stamina_efficiency} attribute. Used by
		 * {@link ParagliderPlayerStates#PARAGLIDING}, {@link ParagliderPlayerStates#PANIC_PARAGLIDING}, and
		 * {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation IS_PARAGLIDING = ParagliderAPI.id("is_paragliding");

		/**
		 * No inherent functionality. Used by {@link ParagliderPlayerStates#SWIMMING} and
		 * {@link ParagliderPlayerStates#RUNNING}.
		 */
		ResourceLocation IS_SPRINTING = ParagliderAPI.id("is_sprinting");

		/**
		 * No inherent functionality. Used by {@link ParagliderPlayerStates#SWIMMING},
		 * {@link ParagliderPlayerStates#UNDERWATER}, and {@link ParagliderPlayerStates#BREATHING_UNDERWATER}.
		 */
		ResourceLocation IS_UNDERWATER = ParagliderAPI.id("is_underwater");
	}
}
