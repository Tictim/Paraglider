package tictim.paraglider.impl.stamina;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class BotWStaminaData {
	public static final MapCodec<BotWStaminaData> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			Codec.DOUBLE.fieldOf("stamina").forGetter(d -> d.stamina),
			Codec.DOUBLE.optionalFieldOf("extraStamina", 0.0).forGetter(d -> d.extraStamina),
			Codec.BOOL.fieldOf("depleted").forGetter(d -> d.depleted)
	).apply(b, BotWStaminaData::new));

	public double stamina;
	public double extraStamina;
	public boolean depleted;

	public BotWStaminaData() {}
	public BotWStaminaData(double stamina, double extraStamina, boolean depleted) {
		this.stamina = stamina;
		this.extraStamina = extraStamina;
		this.depleted = depleted;
	}
}
