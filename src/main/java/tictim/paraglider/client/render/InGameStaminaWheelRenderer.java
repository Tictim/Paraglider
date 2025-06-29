package tictim.paraglider.client.render;

import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelAnimationTracker.UpdateMode.*;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public class InGameStaminaWheelRenderer extends StaminaWheelRenderer {
	private static final InGameStaminaWheelRenderer instance = new InGameStaminaWheelRenderer();

	public static InGameStaminaWheelRenderer get() {
		return instance;
	}

	private final StaminaWheelAnimationTracker fullAnim = new StaminaWheelAnimationTracker();
	private final StaminaWheelAnimationTracker extraWheelFillAnim = new StaminaWheelAnimationTracker(EXTRA_WHEEL_FILL_DURATION);
	private final StaminaWheelAnimationTracker extraWheelEmptyAnim = new StaminaWheelAnimationTracker(EXTRA_WHEEL_EMPTY_DURATION);
	private final StaminaWheelAnimationTracker recoverAnim = new StaminaWheelAnimationTracker(GLOW_FADE_END);

	private boolean prevDepleted;
	private int prevWheelIndex = -1;

	public InGameStaminaWheelRenderer() {
		reset();
	}

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel, float partialTicks) {
		Stamina s = Stamina.get(player);
		double maxStamina = s.maxStamina();
		double stamina = s.stamina();

		Movement movement = Movement.get(player);
		int staminaDelta = movement.staminaDelta();

		wheel.setProperties(stamina, maxStamina);

		boolean full = stamina >= maxStamina;
		int wheelIndex = (int)Math.ceil(wheel.staminaWheelPos());

		this.fullAnim.update(full);
		this.extraWheelFillAnim.update(full ? SET_INACTIVE : this.prevWheelIndex < wheelIndex ? SET_ACTIVE : RETAIN);
		this.extraWheelEmptyAnim.update(full ? SET_INACTIVE : this.prevWheelIndex > wheelIndex ? SET_ACTIVE : RETAIN);
		this.recoverAnim.update(full ? SET_INACTIVE : this.prevDepleted && !s.isDepleted() ? SET_ACTIVE : RETAIN);

		this.prevWheelIndex = wheelIndex;
		this.prevDepleted = s.isDepleted();

		if (full) {
			int color = this.fullAnim.getGlowAndFadeColor(wheelColor(0));
			if (ARGB.alpha(color) <= 0) return;
			wheel.fillStamina(0, maxStamina, color);
			makeExtraWheel(wheel);
		} else {
			wheel.fillStamina(0, maxStamina, EMPTY);
			if (s.isDepleted()) {
				wheel.fillStamina(0, stamina, this.recoverAnim.getGlowColor(getBlinkColor(ms(), true)));
			} else {
				wheel.fillStamina(0, stamina, this.recoverAnim.getGlowColor(wheelColor(0)));
				makeExtraWheel(wheel);

				if (staminaDelta < 0) {
					long ms = ms();
					int color = this.recoverAnim.getGlowColor(getBlinkColor(ms, false));
					wheel.fillStamina(stamina + staminaDelta * 10, stamina, color);

					if (wheel.staminaWheelPos() > 3) {
						wheel.fillWheel(
								1 + toWheelPos(stamina + staminaDelta * 10),
								1 + toWheelPos(stamina), color);
					}
				}
			}
		}

		debugAnim("full", this.fullAnim);
		debugAnim("extraWheelFill", this.extraWheelFillAnim);
		debugAnim("extraWheelEmpty", this.extraWheelEmptyAnim);
		debugAnim("recoverAnim", this.recoverAnim);
	}

	private void makeExtraWheel(Wheel wheel) {
		float staminaWheelPos = wheel.staminaWheelPos();
		if (staminaWheelPos <= 2) return;

		int wheels = (int)Math.ceil(staminaWheelPos);
		int color = wheelColor(wheels - 3);
		int wheelIndicatorColor = wheels == 3 ? 0 : color;

		if (this.extraWheelEmptyAnim.isActive()) {
			float d = Math.min(1, (float)this.extraWheelEmptyAnim.activeDuration() / EXTRA_WHEEL_EMPTY_DURATION);
			color = ARGB.lerp(d, wheelBgColor(wheels - 3), color);
			wheelIndicatorColor = ARGB.lerp(d, wheelColor(wheels - 2),
					wheels == 3 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 3));
		}

		if (this.fullAnim.isActive()) {
			color = this.fullAnim.getGlowAndFadeColor(color);
			if (wheels >= 4) wheelIndicatorColor = color;
		} else if (this.recoverAnim.isActive()) {
			wheelIndicatorColor = color = this.recoverAnim.getGlowColor(color);
		}

		wheel.fillWheel(2, staminaWheelPos, color);

		if (wheels >= 4) {
			int bgColor = wheelBgColor(wheels - 4);

			if (this.extraWheelFillAnim.isActive()) {
				float d = Math.min(1, (float)this.extraWheelFillAnim.activeDuration() / EXTRA_WHEEL_FILL_DURATION);
				bgColor = ARGB.lerp(d, wheelColor(wheels - 4), bgColor);
				wheelIndicatorColor = ARGB.lerp(d,
						wheels == 4 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 4),
						wheelColor(wheels - 3));
			}

			if (this.fullAnim.isActive()) {
				bgColor = this.fullAnim.getGlowAndFadeColor(bgColor);
			} else if (this.recoverAnim.isActive()) {
				bgColor = this.recoverAnim.getGlowColor(bgColor);
			}

			wheel.fillWheel(staminaWheelPos, (float)Math.ceil(staminaWheelPos), bgColor);
		}

		wheel.setExtraWheelIndicatorColor(wheelIndicatorColor);
	}

	public void reset() {
		this.fullAnim.reset();
		this.fullAnim.setActive(true);
		this.fullAnim.setActiveDuration(FADE_END);

		this.extraWheelFillAnim.reset();
		this.extraWheelEmptyAnim.reset();
		this.recoverAnim.reset();

		this.prevDepleted = false;
		this.prevWheelIndex = -1;
	}
}
