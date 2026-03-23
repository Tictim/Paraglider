package tictim.paraglider.api.movement;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.Stamina;

/**
 * Predicate for player state connections. Conditions are checked once per connection,
 */
@NullMarked
@FunctionalInterface
public interface PlayerStateCondition {
	boolean test(Context context);

	interface Context {
		/**
		 * @return The subject which player state is being evaluated
		 */
		ServerPlayer player();

		/**
		 * @return Movement instance associated with the player
		 */
		Movement movement();

		/**
		 * @return Stamina instance associated with the player
		 */
		Stamina stamina();

		/**
		 * @return Previous player state for the player
		 */
		PlayerState prevState();

		boolean paragliding();

		/**
		 * @return Whether you can perform "Panic Paragliding" this tick; "Panic Paragliding" refers to the game
		 * mechanic that enables players to use Paraglider for a brief second after running out of stamina.
		 */
		boolean canDoPanicParagliding();
	}
}
