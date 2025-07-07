package tictim.paraglider.api.stamina;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;

/**
 * <p>
 * Interface providing access to stamina system.
 * </p>
 * <p>
 * The default implementation of this interface used by Paragliders mod is BotW-like stamina system. This can be
 * changed with custom implementation via plugin system; see {@link StaminaPlugin}.
 * </p>
 * <p>
 * The stamina API is written with server-oriented architecture in mind; as result, base mod's stamina implementation
 * does not allow changes made in client-side instance to affect the server-side instance.
 * </p>
 * <p>
 * Use this interface when dealing with discrete changes to stamina value. If the change happens per-tick basis,
 * mediated by a player's state exclusive to other physical actions such as paragliding, running or swimming, consider
 * using custom {@link tictim.paraglider.api.movement.PlayerState PlayerState} instead. If the change happens per-tick
 * basis, but is not based on any player state, use this interface; applying same changes on both server and client side
 * with {@code silent} parameter set to {@code true} can improve both user experience and reduce the amount of sync
 * packets created.
 * </p>
 */
public interface Stamina {
	/**
	 * Amount of stamina represented by one stamina wheel
	 */
	double STAMINA_PER_WHEEL = 1000;

	/**
	 * Get a stamina instance bound to the player.
	 *
	 * @param player Player
	 * @return A stamina instance bound to the player
	 */
	static @NotNull Stamina get(@NotNull Player player) {
		return ParagliderAPI.staminaSupplier().apply(player);
	}

	/**
	 * @return Amount of stamina
	 */
	double stamina();

	/**
	 * @param stamina Amount of stamina to be set
	 * @see #setStamina(double, boolean)
	 */
	default void setStamina(double stamina) {
		setStamina(stamina, false);
	}

	/**
	 * @param stamina Amount of stamina to be set
	 * @param silent  If {@code true}, changes will not be sent to the client on server-side
	 */
	void setStamina(double stamina, boolean silent);

	/**
	 * @return Maximum amount of stamina, >=0
	 */
	double maxStamina();

	/**
	 * @return Extra stamina, the value represented by yellow stamina wheels left to the main wheel
	 */
	double extraStamina();

	/**
	 * @param extraStamina Amount of extra stamina, the value represented by yellow stamina wheels left to the main wheel
	 */
	default void setExtraStamina(double extraStamina) {
		setExtraStamina(extraStamina, false);
	}

	/**
	 * @param extraStamina Amount of extra stamina, the value represented by yellow stamina wheels left to the main wheel
	 * @param silent       If {@code true}, changes will not be sent to the client on server-side
	 */
	void setExtraStamina(double extraStamina, boolean silent);

	/**
	 * @return Whether depleted state is active
	 */
	boolean isDepleted();

	/**
	 * @param depleted Whether depleted state should be active
	 */
	default void setDepleted(boolean depleted) {
		setDepleted(depleted, false);
	}

	void setDepleted(boolean depleted, boolean silent);

	/**
	 * @return Whether this stamina instance needs to sync its state to client
	 */
	boolean isDirty();

	/**
	 * @param dirty Whether this stamina instance needs to sync its state to client
	 */
	void setDirty(boolean dirty);

	/**
	 * Tries to add stamina by specific {@code amount} without exceeding {@link Stamina#maxStamina() maxStamina}.
	 *
	 * @param amount   Amount of stamina to be given
	 * @param simulate If {@code true}, this method call does not affect the game state; instead the return value is
	 *                 evaluated only as a simulated result.
	 * @return Amount of stamina given
	 * @see #giveStamina(double, boolean, boolean)
	 */
	default double giveStamina(double amount, boolean simulate) {
		return giveStamina(amount, simulate, false);
	}

	/**
	 * Tries to add stamina by specific {@code amount} without exceeding {@link Stamina#maxStamina() maxStamina}.
	 *
	 * @param amount   Amount of stamina to be given
	 * @param simulate If {@code true}, this method call does not affect the game state; instead the return value is
	 *                 evaluated only as a simulated result.
	 * @param silent   If {@code true}, changes will not be sent to the client on server-side
	 * @return Amount of stamina given
	 */
	double giveStamina(double amount, boolean simulate, boolean silent);

	/**
	 * <p>
	 * Subtract stamina by specific {@code amount}. If stamina is currently in depleted state, unless
	 * {@code ignoreDepletion} is {@code true}, no stamina will be subtracted.
	 * </p>
	 * <p>
	 * This method will also use {@link #extraStamina()}. Base stamina will always be used before extra stamina.
	 * </p>
	 *
	 * @param amount          Amount of stamina to be taken
	 * @param simulate        If {@code true}, this method call does not affect the game state; instead the return value is
	 *                        evaluated only as a simulated result.
	 * @param ignoreDepletion Bypasses depleted state check if {@code true}
	 * @return Amount of stamina taken
	 * @see #takeStamina(double, boolean, boolean, boolean, boolean, boolean)
	 */
	default double takeStamina(double amount, boolean simulate, boolean ignoreDepletion) {
		return takeStamina(amount, simulate, ignoreDepletion, false);
	}

	/**
	 * <p>
	 * Subtract stamina by specific {@code amount}. If stamina is currently in depleted state, unless
	 * {@code ignoreDepletion} is {@code true}, no stamina will be subtracted.
	 * </p>
	 * <p>
	 * This method will also use {@link #extraStamina()}. Base stamina will always be used before extra stamina.
	 * </p>
	 *
	 * @param amount          Amount of stamina to be taken
	 * @param simulate        If {@code true}, this method call does not affect the game state; instead the return value is
	 *                        evaluated only as a simulated result.
	 * @param ignoreDepletion Bypasses depleted state check if {@code true}
	 * @param silent          If {@code true}, changes will not be sent to the client on server-side
	 * @return Amount of stamina taken
	 * @see #takeStamina(double, boolean, boolean, boolean, boolean, boolean)
	 */
	default double takeStamina(double amount, boolean simulate, boolean ignoreDepletion, boolean silent) {
		return takeStamina(amount, simulate, ignoreDepletion, true, true, silent);
	}

	/**
	 * <p>
	 * Subtract stamina by specific {@code amount}. If stamina is currently in depleted state, unless
	 * {@code ignoreDepletion} is {@code true}, no stamina will be subtracted.
	 * </p>
	 * <p>
	 * If {@code takeExtraStamina} is {@code true}, this method will also use {@link #extraStamina()}. Base stamina will
	 * always be used before extra stamina.
	 * </p>
	 *
	 * @param amount           Amount of stamina to be taken
	 * @param simulate         If {@code true}, this method call does not affect the game state; instead the return value is
	 *                         evaluated only as a simulated result.
	 * @param ignoreDepletion  Bypasses depleted state check if {@code true}
	 * @param takeBaseStamina  Whether to use {@link #stamina()} or not
	 * @param takeExtraStamina Whether to use {@link #extraStamina()} or not
	 * @param silent           If {@code true}, changes will not be sent to the client on server-side
	 * @return Amount of stamina taken
	 */
	double takeStamina(double amount, boolean simulate, boolean ignoreDepletion, boolean takeBaseStamina, boolean takeExtraStamina, boolean silent);

	/**
	 * Renders stamina wheel if this value is {@code true}. Client side only.
	 *
	 * @return Whether the stamina wheel should be rendered using this instance
	 */
	default boolean renderStaminaWheel() {
		return true;
	}

	/**
	 * Whether to update this stamina instance with Paraglider's default logic. Return {@code false} to disable.
	 */
	default boolean updateWithDefaultLogic(boolean client) {
		return true;
	}

	/**
	 * Sync properties from Paraglider's packet.
	 */
	default void syncProperties(double stamina, double extraStamina, boolean depleted) {
		setStamina(stamina);
		setExtraStamina(extraStamina);
		setDepleted(depleted);
	}
}
