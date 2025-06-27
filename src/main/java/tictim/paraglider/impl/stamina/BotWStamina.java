package tictim.paraglider.impl.stamina;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

/**
 * Implementation of in-house stamina system modeled after BotW's stamina feature.
 */
public class BotWStamina implements Stamina {
	public static final Codec<BotWStamina> CODEC = RecordCodecBuilder.create(b -> b.group(
			Codec.INT.fieldOf("staminaVessels").forGetter(BotWStamina::staminaVessels),
			Codec.INT.fieldOf("stamina").forGetter(BotWStamina::stamina),
			Codec.BOOL.fieldOf("depleted").forGetter(BotWStamina::isDepleted)
	).apply(b, BotWStamina::new));

	private int staminaVessels;
	private int stamina;
	private boolean depleted;

	public BotWStamina(@Nullable VesselContainer vessels) {
		this.staminaVessels = vessels != null ? vessels.staminaVessel() : 0;
		this.stamina = maxStamina();
	}

	public BotWStamina(int staminaVessels, int stamina, boolean depleted) {
		this.staminaVessels = staminaVessels;
		this.stamina = stamina;
		this.depleted = depleted;
	}

	@Override public int stamina() {
		return stamina;
	}
	@Override public void setStamina(int stamina) {
		this.stamina = stamina;
	}
	@Override public int maxStamina() {
		return Cfg.get().maxStamina(this.staminaVessels);
	}
	@Override public boolean isDepleted() {
		return depleted;
	}
	@Override public void setDepleted(boolean depleted) {
		this.depleted = depleted;
	}

	public int staminaVessels() {
		return this.staminaVessels;
	}
	@Override public void setStaminaVessels(int staminaVessels) {
		this.staminaVessels = staminaVessels;
	}

	@Override public int giveStamina(int amount, boolean simulate) {
		if (amount <= 0) return 0;
		int staminaToGive = Math.min(amount, maxStamina() - this.stamina);
		if (staminaToGive <= 0) return 0;
		if (!simulate) this.stamina += staminaToGive;
		return staminaToGive;
	}

	@Override public int takeStamina(int amount, boolean simulate, boolean ignoreDepletion) {
		if (amount <= 0 || (isDepleted() && !ignoreDepletion)) return 0;
		int staminaToTake = Math.min(amount, this.stamina);
		if (staminaToTake <= 0) return 0;
		if (!simulate) this.stamina -= staminaToTake;
		return staminaToTake;
	}
}
