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

	private boolean dirty;

	public BotWStamina(Player player) {
		this.player = player;
		this.data = player.getData(Contents.get().botwStaminaData());
	}

	@Override public double stamina() {
		return this.data.stamina;
	}
	@Override public void setStamina(double stamina, boolean silent) {
		if (this.data.stamina != stamina) {
			this.data.stamina = stamina;
			if (!silent) this.dirty = true;
		}
	}

	@Override public double maxStamina() {
		return this.player.getAttributeValue(Contents.get().maxStamina());
	}

	@Override public double extraStamina() {
		return this.data.extraStamina;
	}
	@Override public void setExtraStamina(double extraStamina, boolean silent) {
		if (this.data.extraStamina != extraStamina) {
			this.data.extraStamina = extraStamina;
			if (!silent) this.dirty = true;
		}
	}

	@Override public boolean isDepleted() {
		return this.data.depleted;
	}
	@Override public void setDepleted(boolean depleted, boolean silent) {
		if (this.data.depleted != depleted) {
			this.data.depleted = depleted;
			if (!silent) this.dirty = true;
		}
	}

	@Override public boolean isDirty() {
		return this.dirty;
	}
	@Override public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@Override public double giveStamina(double amount, boolean simulate, boolean silent) {
		if (!Double.isFinite(amount) || amount <= 0) return 0;

		double stamina = this.data.stamina;
		double maxStamina = maxStamina();
		double space = maxStamina - stamina;
		if (space <= 0) return 0;

		if (!silent) this.dirty = true;

		if (space >= amount) {
			if (!simulate) this.data.stamina = stamina + amount;
			return amount;
		} else {
			if (!simulate) this.data.stamina = maxStamina;
			return space;
		}
	}

	@Override public double takeStamina(
			double amount, boolean simulate, boolean ignoreDepletion,
			boolean takeBaseStamina, boolean takeExtraStamina, boolean silent
	) {
		if (!Double.isFinite(amount) || amount <= 0 || (isDepleted() && !ignoreDepletion)) return 0;

		double staminaTaken = 0;

		if (takeBaseStamina) {
			staminaTaken = takeBaseStamina(amount, simulate);
			if (staminaTaken >= amount) {
				if (!silent) this.dirty = true;
				return amount;
			}
		}

		if (takeExtraStamina) {
			staminaTaken += takeExtraStamina(amount - staminaTaken, simulate);
		}

		if (staminaTaken != 0 && !silent) this.dirty = true;
		return staminaTaken;
	}

	private double takeBaseStamina(double amount, boolean simulate) {
		double stamina = this.data.stamina;

		if (stamina >= amount) {
			if (!simulate) this.data.stamina = stamina - amount;
			return amount;
		} else {
			if (!simulate) this.data.stamina = 0;
			return stamina;
		}
	}

	private double takeExtraStamina(double amount, boolean simulate) {
		double extraStamina = this.data.extraStamina;

		if (extraStamina >= amount) {
			if (!simulate) this.data.extraStamina = extraStamina - amount;
			return amount;
		} else {
			if (!simulate) this.data.extraStamina = 0;
			return extraStamina;
		}
	}
}
