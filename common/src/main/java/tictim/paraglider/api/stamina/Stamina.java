package tictim.paraglider.api.stamina;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.Movement;

/**
 * <p>
 * Interface providing access to stamina system.
 * </p>
 * <p>
 * The default implementation of this interface used by Paragliders mod is BotW-like stamina system. This can be
 * changed with custom implementation via plugin system; see {@link StaminaPlugin}.
 * </p>
 * <p>
 * In the base mod, the stamina implementation is server-oriented; changes made in client-side instance do not affect
 * the server-side instance, and could even cause state desync between the two sides. Manual state sync is required if
 * you want to sync other values, aside from the stamina value, which can get synced with built-in packets; see the base
 * implementation for details.
 * </p>
 * <p>
 * If stamina is gained/consumed each tick based on certain player state, then using
 * {@link tictim.paraglider.api.movement.PlayerState PlayerState} is recommended.
 * </p>
 */
public interface Stamina{
	/**
	 * Get a stamina instance bound to specific player; if there's none, a no-op singleton implementation is returned.
	 *
	 * @param player Player
	 * @return A stamina instance bound to specific player, or a no-op singleton implementation
	 */
	@NotNull static Stamina get(@NotNull Player player){
		return ParagliderAPI.staminaSupplier().apply(player);
	}

	/**
	 * @return Amount of stamina
	 */
	int stamina();

	/**
	 * @param stamina Amount of stamina to be set
	 */
	void setStamina(int stamina);

	/**
	 * @return Maximum amount of stamina, >=0
	 */
	int maxStamina();

	/**
	 * @return Whether depleted state is active
	 */
	boolean isDepleted();

	/**
	 * @param depleted Whether depleted state should be active
	 */
	void setDepleted(boolean depleted);

	/**
	 * Update state of {@code this} with state of {@code movement}.
	 *
	 * @param movement Movement instance
	 */
	void update(@NotNull Movement movement);

	/**
	 * Tries to add stamina by specific {@code amount} without exceeding {@link Stamina#maxStamina() maxStamina}.
	 *
	 * @param amount   Amount of stamina to be given
	 * @param simulate If {@code true}, this method call does not affect the game state; instead the return value is
	 *                 evaluated only as a simulated result.
	 * @return Amount of stamina given
	 */
	int giveStamina(int amount, boolean simulate);

	/**
	 * Subtract stamina by specific {@code amount}. If stamina is currently in depleted state, unless
	 * {@code ignoreDepletion} is {@code true}, no stamina will be subtracted.
	 *
	 * @param amount          Amount of stamina to be taken
	 * @param simulate        If {@code true}, this method call does not affect the game state; instead the return value is
	 *                        evaluated only as a simulated result.
	 * @param ignoreDepletion Bypasses depleted state check if {@code true}
	 * @return Amount of stamina taken
	 */
	int takeStamina(int amount, boolean simulate, boolean ignoreDepletion);

	/**
	 * Renders stamina wheel if this value is {@code true}. Client side only.
	 *
	 * @return Whether the stamina wheel should be rendered using this instance
	 */
	@Environment(EnvType.CLIENT)
	default boolean renderStaminaWheel(){
		return true;
	}
}
