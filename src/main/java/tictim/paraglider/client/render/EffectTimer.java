package tictim.paraglider.client.render;

import org.jspecify.annotations.NullMarked;

import static tictim.paraglider.ParagliderUtils.ms;

@NullMarked
public class EffectTimer {
	private final long finishAt;

	private boolean active;
	private long activeTime;
	private long activeDuration;

	public EffectTimer(long finishAt) {
		this.finishAt = finishAt;
	}
	public EffectTimer() {
		this(-1);
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive() {
		setActive(0);
	}

	public void setActive(long activeDuration) {
		this.active = true;
		this.activeTime = ms() - activeDuration;
		this.activeDuration = activeDuration;
	}

	public long activeTime() {
		return this.activeTime;
	}

	public long activeDuration() {
		return this.activeDuration;
	}
	public void setActiveDuration(long activeDuration) {
		this.activeDuration = activeDuration;
	}

	public int getGlowColor(int baseColor) {
		return this.active ? StaminaWheelConstants.getGlowColor(this.activeDuration, baseColor) : baseColor;
	}

	public float getFadeAlpha() {
		return this.active ? StaminaWheelConstants.getFadeAlpha(this.activeDuration) : 1;
	}

	public void update(boolean active) {
		update(active ? UpdateMode.RETAIN_ACTIVE : UpdateMode.SET_INACTIVE);
	}

	public void update(UpdateMode active) {
		switch (active) {
			case RETAIN_ACTIVE:
				if (this.active) {
					updateActiveDuration();
					return;
				}
			case SET_ACTIVE:
				this.active = true;
				this.activeTime = ms();
				this.activeDuration = 0;
				return;
			case SET_INACTIVE:
				this.active = false;
				this.activeTime = 0;
				this.activeDuration = 0;
				return;
			case RETAIN:
				if (this.active) updateActiveDuration();
		}
	}

	private void updateActiveDuration() {
		this.activeDuration = ms() - this.activeTime;
		if (this.finishAt >= 0 && this.activeDuration > this.finishAt) {
			this.active = false;
			this.activeTime = 0;
			this.activeDuration = 0;
		}
	}

	public void reset() {
		this.active = false;
		this.activeTime = 0;
		this.activeDuration = 0;
	}

	public enum UpdateMode {
		SET_ACTIVE,
		RETAIN_ACTIVE,
		SET_INACTIVE,
		RETAIN
	}
}
