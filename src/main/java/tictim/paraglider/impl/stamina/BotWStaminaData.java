package tictim.paraglider.impl.stamina;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class BotWStaminaData {
	public static final Codec<BotWStaminaData> CODEC = RecordCodecBuilder.create(b -> b.group(
			Codec.DOUBLE.fieldOf("stamina").forGetter(BotWStaminaData::stamina),
			Codec.BOOL.fieldOf("depleted").forGetter(BotWStaminaData::isDepleted)
	).apply(b, BotWStaminaData::new));

	private double stamina;
	private boolean depleted;

	public BotWStaminaData() {}
	public BotWStaminaData(double stamina, boolean depleted) {
		this.stamina = stamina;
		this.depleted = depleted;
	}

	public double stamina() {
		return stamina;
	}
	public void setStamina(double stamina) {
		this.stamina = stamina;
	}
	public boolean isDepleted() {
		return depleted;
	}
	public void setDepleted(boolean depleted) {
		this.depleted = depleted;
	}
}
