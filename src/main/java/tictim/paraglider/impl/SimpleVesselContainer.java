package tictim.paraglider.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard implementation of {@link VesselContainer}.
 */
public class SimpleVesselContainer implements VesselContainer {
	public static final MapCodec<SimpleVesselContainer> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			Codec.INT.fieldOf("heartContainers").forGetter(SimpleVesselContainer::heartContainer),
			Codec.INT.fieldOf("staminaVessels").forGetter(SimpleVesselContainer::staminaVessel),
			Codec.INT.fieldOf("essences").forGetter(SimpleVesselContainer::essence)
	).apply(b, SimpleVesselContainer::new));

	protected int heartContainer;
	protected int staminaVessel;
	protected int essence;

	protected @Nullable List<OnChangeListener> listeners;

	public SimpleVesselContainer() {}
	public SimpleVesselContainer(int heartContainer, int staminaVessel, int essence) {
		this.heartContainer = heartContainer;
		this.staminaVessel = staminaVessel;
		this.essence = essence;
	}

	@Override public final int heartContainer() {
		return heartContainer;
	}
	@Override public final int staminaVessel() {
		return staminaVessel;
	}
	@Override public final int essence() {
		return essence;
	}

	@Override public @NotNull SetResult setHeartContainer(int amount, boolean simulate, boolean playEffect) {
		if (amount < 0) return SetResult.TOO_LOW;
		if (amount > Cfg.get().maxHeartContainers()) return SetResult.TOO_HIGH;
		int change = amount - this.heartContainer;
		if (change == 0) return SetResult.NO_CHANGE;
		if (!simulate) {
			this.heartContainer = amount;
			onChange(ActionType.HEART_CONTAINER, change, playEffect);
		}
		return SetResult.OK;
	}

	@Override public @NotNull SetResult setStaminaVessel(int amount, boolean simulate, boolean playEffect) {
		if (amount < 0) return SetResult.TOO_LOW;
		if (amount > Cfg.get().maxStaminaVessels()) return SetResult.TOO_HIGH;
		int change = amount - this.staminaVessel;
		if (change == 0) return SetResult.NO_CHANGE;
		if (!simulate) {
			this.staminaVessel = amount;
			onChange(ActionType.STAMINA_VESSEL, change, playEffect);
		}
		return SetResult.OK;
	}

	@Override public @NotNull SetResult setEssence(int amount, boolean simulate, boolean playEffect) {
		if (amount < 0) return SetResult.TOO_LOW;
		int change = amount - this.essence;
		if (change == 0) return SetResult.NO_CHANGE;
		if (!simulate) {
			this.essence = amount;
			onChange(ActionType.ESSENCE, change, playEffect);
		}
		return SetResult.OK;
	}

	@Override public int giveHeartContainers(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, Cfg.get().maxHeartContainers() - this.heartContainer);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.heartContainer += amount;
			onChange(ActionType.HEART_CONTAINER, amount, playEffect);
		}
		return amount;
	}

	@Override public int giveStaminaVessels(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, Cfg.get().maxStaminaVessels() - this.staminaVessel);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.staminaVessel += amount;
			onChange(ActionType.STAMINA_VESSEL, amount, playEffect);
		}
		return amount;
	}

	@Override public int giveEssences(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, Integer.MAX_VALUE - this.essence);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.essence += amount;
			onChange(ActionType.ESSENCE, amount, playEffect);
		}
		return amount;
	}

	@Override public int takeHeartContainers(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, this.heartContainer);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.heartContainer -= amount;
			onChange(ActionType.HEART_CONTAINER, -amount, playEffect);
		}
		return amount;
	}

	@Override public int takeStaminaVessels(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, this.staminaVessel);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.staminaVessel -= amount;
			onChange(ActionType.STAMINA_VESSEL, -amount, playEffect);
		}
		return amount;
	}

	@Override public int takeEssences(int amount, boolean simulate, boolean playEffect) {
		amount = Math.min(amount, this.essence);
		if (amount <= 0) return 0;
		if (!simulate) {
			this.essence -= amount;
			onChange(ActionType.ESSENCE, -amount, playEffect);
		}
		return amount;
	}

	protected void onChange(@NotNull ActionType actionType, int change, boolean playEffect) {
		if (this.listeners != null) {
			for (OnChangeListener listener : this.listeners) {
				listener.onChange(actionType, change, playEffect);
			}
		}
	}

	@Override public void onChange(@NotNull OnChangeListener onChangeListener) {
		if (this.listeners == null) this.listeners = new ArrayList<>();
		this.listeners.add(onChangeListener);
	}

	@Override public void unregisterOnChange(@NotNull OnChangeListener onChangeListener) {
		if (this.listeners != null) this.listeners.remove(onChangeListener);
	}

	@Override public String toString() {
		return "SimpleVesselContainer{" +
				"heartContainer=" + heartContainer +
				", staminaVessel=" + staminaVessel +
				", essence=" + essence +
				'}';
	}
}
