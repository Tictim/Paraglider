package tictim.paraglider.impl.stamina;

import net.minecraft.world.entity.player.Player;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.contents.Contents;

/**
 * Implementation of in-house stamina system modeled after BotW's stamina feature.
 */
public class BotWStamina implements Stamina {
	private final Player player;
	private final BotWStaminaData data;

	public BotWStamina(Player player) {
		this.player = player;
		this.data = player.getData(Contents.get().botwStaminaData());
	}

	private BotWStaminaData data() {
		return this.data;
	}

	@Override public double stamina() {
		return this.data.stamina();
	}
	@Override public void setStamina(double stamina) {
		this.data.setStamina(stamina);
	}
	@Override public double maxStamina() {
		return this.player.getAttributeValue(Contents.get().maxStamina());
	}
	@Override public boolean isDepleted() {
		return this.data.isDepleted();
	}
	@Override public void setDepleted(boolean depleted) {
		this.data.setDepleted(depleted);
	}

	@Override public double giveStamina(double amount, boolean simulate) {
		if (!Double.isFinite(amount) || amount <= 0) return 0;

		double stamina = this.data.stamina();
		double maxStamina = maxStamina();
		double space = maxStamina - stamina;
		if (space <= 0) return 0;

		if (space >= amount) {
			if (!simulate) this.data.setStamina(stamina + amount);
			return amount;
		} else {
			if (!simulate) this.data.setStamina(maxStamina);
			return space;
		}
	}

	@Override public double takeStamina(double amount, boolean simulate, boolean ignoreDepletion) {
		if (!Double.isFinite(amount) || amount <= 0 || (isDepleted() && !ignoreDepletion)) return 0;

		double stamina = this.data.stamina();

		if (stamina >= amount) {
			if (!simulate) this.data.setStamina(stamina - amount);
			return amount;
		} else {
			if (!simulate) this.data.setStamina(0);
			return stamina;
		}
	}
}
