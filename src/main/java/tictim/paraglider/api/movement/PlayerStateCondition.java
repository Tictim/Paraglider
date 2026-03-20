package tictim.paraglider.api.movement;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;

/**
 * Predicate for player state connections. Conditions are checked once per connection,
 */
@FunctionalInterface
public interface PlayerStateCondition {
	boolean test(@NotNull Context context);

	interface Context {
		/**
		 * @return The subject which player state is being evaluated
		 */
		@NotNull ServerPlayer player();

		/**
		 * @return Movement instance associated with the player
		 */
		@NotNull Movement movement();

		/**
		 * @return Stamina instance associated with the player
		 */
		@NotNull Stamina stamina();

		/**
		 * @return Previous player state for the player
		 */
		@NotNull PlayerState prevState();

		boolean paragliding();

		/**
		 * @return Whether you can perform "Panic Paragliding" this tick; "Panic Paragliding" refers to the game
		 * mechanic that enables players to use Paraglider for a brief second after running out of stamina.
		 */
		boolean canDoPanicParagliding();
	}
}
