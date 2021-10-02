package tictim.paraglider.capabilities;

public interface Stamina{
	/**
	 * @return Amount of stamina
	 */
	int getStamina();

	/**
	 * @param stamina Amount of stamina to be set
	 */
	void setStamina(int stamina);

	/**
	 * @return Maximum amount of stamina, >=0
	 */
	int getMaxStamina();

	/**
	 * @return Whether or not depleted state is active
	 */
	boolean isDepleted();

	/**
	 * @param depleted Whether or not depleted state should be active
	 */
	void setDepleted(boolean depleted);

	/**
	 * Tries to add stamina by specific {@code amount} without exceeding {@link Stamina#getMaxStamina() maxStamina}.
	 *
	 * @param amount   Amount of stamina to be given
	 * @param simulate Simulation only if {@code true}
	 * @return Amount of stamina given
	 */
	int giveStamina(int amount, boolean simulate);

	/**
	 * Subtract stamina by specific {@code amount}. If stamina is currently in depleted state, unless {@code ignoreDepletion} is {@code true}, no stamina will be subtracted.
	 *
	 * @param amount          Amount of stamina to be taken
	 * @param simulate        Simulation only if {@code true}
	 * @param ignoreDepletion Bypasses depleted state check if {@code true}
	 * @return Amount of stamina taken
	 */
	int takeStamina(int amount, boolean simulate, boolean ignoreDepletion);
}
