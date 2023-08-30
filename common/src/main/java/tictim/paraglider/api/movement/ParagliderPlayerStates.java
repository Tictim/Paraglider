package tictim.paraglider.api.movement;

import net.minecraft.resources.ResourceLocation;
import tictim.paraglider.api.ParagliderAPI;

/**
 * Default set of player states provided by Paragliders mod.
 */
public interface ParagliderPlayerStates{
	/**
	 * Default value for {@link PlayerState#recoveryDelay()}.
	 */
	int RECOVERY_DELAY = 10;

	ResourceLocation IDLE = ParagliderAPI.id("idle");
	int IDLE_STAMINA_DELTA = 20;

	ResourceLocation FLYING = ParagliderAPI.id("flying");
	int FLYING_STAMINA_DELTA = IDLE_STAMINA_DELTA;
	double FLYING_PRIORITY = 7;

	ResourceLocation ON_VEHICLE = ParagliderAPI.id("on_vehicle");
	int ON_VEHICLE_STAMINA_DELTA = IDLE_STAMINA_DELTA;
	double ON_VEHICLE_PRIORITY = 6;

	ResourceLocation SWIMMING = ParagliderAPI.id("swimming");
	int SWIMMING_STAMINA_DELTA = -6;
	double SWIMMING_PRIORITY = 5;

	ResourceLocation UNDERWATER = ParagliderAPI.id("underwater");
	int UNDERWATER_STAMINA_DELTA = 3;
	double UNDERWATER_PRIORITY = 4;

	ResourceLocation BREATHING_UNDERWATER = ParagliderAPI.id("breathing_underwater");
	int BREATHING_UNDERWATER_STAMINA_DELTA = IDLE_STAMINA_DELTA;

	ResourceLocation PARAGLIDING = ParagliderAPI.id("paragliding");
	int PARAGLIDING_STAMINA_DELTA = -3;
	double PARAGLIDING_PRIORITY = 3;

	ResourceLocation PANIC_PARAGLIDING = ParagliderAPI.id("panic_paragliding");
	int PANIC_PARAGLIDING_STAMINA_DELTA = -3;

	ResourceLocation ASCENDING = ParagliderAPI.id("ascending");
	int ASCENDING_STAMINA_DELTA = PARAGLIDING_STAMINA_DELTA;

	ResourceLocation RUNNING = ParagliderAPI.id("running");
	int RUNNING_STAMINA_DELTA = -10;
	double RUNNING_PRIORITY = 2;

	ResourceLocation MIDAIR = ParagliderAPI.id("midair");
	int MIDAIR_STAMINA_DELTA = 0;
	double MIDAIR_PRIORITY = 1;

	/**
	 * Default set of player state flags used by Paraglider mod.
	 */
	interface Flags{
		/**
		 * This flag achieves two things:
		 * <ul>
		 * <li>
		 * Marks the state as paragliding state, which will be used to  alter visuals on paraglider items and such.
		 * </li>
		 * <li>
		 * Additionally, if the config {@code paraglidingConsumesStamina} is set to {@code false}, negative stamina
		 * delta will be considered as zero.
		 * </li>
		 * </ul>
		 * <p/>
		 * Used by {@link ParagliderPlayerStates#PARAGLIDING} and {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation FLAG_PARAGLIDING = PARAGLIDING;
		/**
		 * If the config {@code runningConsumesStamina} is set to {@code false}, and the state has negative stamina
		 * delta, it will be considered as zero.
		 * <p/>
		 * Used by {@link ParagliderPlayerStates#SWIMMING}, {@link ParagliderPlayerStates#UNDERWATER},
		 * {@link ParagliderPlayerStates#BREATHING_UNDERWATER}, and {@link ParagliderPlayerStates#RUNNING}.
		 */
		ResourceLocation FLAG_RUNNING = RUNNING;
		/**
		 * If a player is on a state marked by this flag and {@link Flags#FLAG_PARAGLIDING}, it will slowly move the
		 * player upwards.
		 * <p/>
		 * Used by {@link ParagliderPlayerStates#ASCENDING}.
		 */
		ResourceLocation FLAG_ASCENDING = ASCENDING;
	}
}
