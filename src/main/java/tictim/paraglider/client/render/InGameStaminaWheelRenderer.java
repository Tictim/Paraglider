package tictim.paraglider.client.render;

import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.EffectTimer.UpdateMode.*;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

@NullMarked
public class InGameStaminaWheelRenderer extends StaminaWheelRenderer {
	private static final InGameStaminaWheelRenderer instance = new InGameStaminaWheelRenderer();

	public static InGameStaminaWheelRenderer get() {
		return instance;
	}

	private final EffectTimer fullAnim = new EffectTimer();
	private final EffectTimer outerWheelFillAnim = new EffectTimer(OUTER_WHEEL_FILL_DURATION);
	private final EffectTimer outerWheelEmptyAnim = new EffectTimer(OUTER_WHEEL_EMPTY_DURATION);
	private final EffectTimer recoverAnim = new EffectTimer(GLOW_FADE_END);
	private final EffectTimer gainExtraStaminaAnim = new EffectTimer(GLOW_FADE_END);

	private boolean prevDepleted;
	private int prevWheelIndex = -1;
	private double prevMaxStamina = Double.NaN;
	private double prevExtraStamina;

	public InGameStaminaWheelRenderer() {
		reset();
	}

	@Override protected void makeWheel(Player player, float partialTicks) {
		Stamina s = Stamina.get(player);
		double maxStamina = s.maxStamina();
		double stamina = s.stamina();
		double extraStamina = s.extraStamina();

		Movement movement = Movement.get(player);
		double staminaDelta = movement.staminaDelta();

		this.mainWheel.setProperties(stamina, maxStamina);
		this.extraWheel.setProperties(extraStamina, extraStamina);

		boolean full = stamina >= maxStamina;
		int wheelIndex = (int)Math.ceil(this.mainWheel.staminaWheelPos());
		boolean gainedExtraStamina = !this.gainExtraStaminaAnim.isActive() && this.prevExtraStamina < extraStamina;

		this.fullAnim.update(full ? gainedExtraStamina || this.prevMaxStamina < maxStamina ? SET_ACTIVE : RETAIN_ACTIVE : SET_INACTIVE);
		this.outerWheelFillAnim.update(full ? SET_INACTIVE : this.prevWheelIndex < wheelIndex ? SET_ACTIVE : RETAIN);
		this.outerWheelEmptyAnim.update(full ? SET_INACTIVE : this.prevWheelIndex > wheelIndex ? SET_ACTIVE : RETAIN);
		this.recoverAnim.update(full ? SET_INACTIVE : this.prevDepleted && !s.isDepleted() ? SET_ACTIVE : RETAIN);
		this.gainExtraStaminaAnim.update(gainedExtraStamina ? SET_ACTIVE : RETAIN);

		debugAnim("full", this.fullAnim);
		debugAnim("outerWheelFill", this.outerWheelFillAnim);
		debugAnim("outerWheelEmpty", this.outerWheelEmptyAnim);
		debugAnim("recoverAnim", this.recoverAnim);
		debugAnim("gainExtraStamina", this.gainExtraStaminaAnim);

		this.prevWheelIndex = wheelIndex;
		this.prevDepleted = s.isDepleted();
		this.prevMaxStamina = maxStamina;
		if (!this.gainExtraStaminaAnim.isActive()) this.prevExtraStamina = extraStamina;

		double staminaDeltaHighlightRemaining = 0;
		int blinkColor = 0;

		if (full) {
			int color = this.fullAnim.getGlowColor(wheelColor(0));
			if (ARGB.alpha(color) <= 0) return;
			this.mainWheel.fillStamina(0, maxStamina, color);
			makeOuterWheel(this.mainWheel);

			float alpha = this.fullAnim.getFadeAlpha();

			this.mainWheel.setAlpha(alpha);
			this.extraWheel.setAlpha(alpha);
		} else {
			if (s.isDepleted()) {
				this.mainWheel.fillStamina(0, stamina, this.recoverAnim.getGlowColor(getBlinkColor(ms(), true)));
			} else {
				this.mainWheel.fillStamina(0, stamina, this.recoverAnim.getGlowColor(wheelColor(0)));
				makeOuterWheel(this.mainWheel);

				if (staminaDelta < 0) {
					blinkColor = getBlinkColor(ms(), false);
					int color = this.recoverAnim.getGlowColor(blinkColor);
					double staminaDeltaHighlightStart = stamina + staminaDelta * 10;
					this.mainWheel.fillStamina(staminaDeltaHighlightStart, stamina, color);

					if (staminaDeltaHighlightStart < 0) {
						staminaDeltaHighlightRemaining = -staminaDeltaHighlightStart;
					}

					if (this.mainWheel.staminaWheelPos() > 3) {
						this.mainWheel.fillWheel(
								1 + toWheelPos(staminaDeltaHighlightStart),
								1 + toWheelPos(stamina), color);
					}
				}
			}
		}

		if (extraStamina > 0) {
			int extraWheelColor = EXTRA;
			this.extraWheel.fillStamina(0, extraStamina, extraWheelColor);

			if (this.gainExtraStaminaAnim.isActive()) {
				extraWheelColor = this.gainExtraStaminaAnim.getGlowColor(EXTRA);
				this.extraWheel.fillStamina(this.prevExtraStamina, extraStamina, extraWheelColor);
			}

			this.extraWheel.setIndicatorColor(extraWheelColor);

			if (staminaDeltaHighlightRemaining > 0) {
				float staminaEndWheePos = toWheelPos(extraStamina);
				this.extraWheel.fillWheel(
						staminaEndWheePos - toWheelPos(staminaDeltaHighlightRemaining),
						staminaEndWheePos, blinkColor);
			}
		}
	}

	private void makeOuterWheel(StaminaWheelState wheel) {
		float staminaWheelPos = wheel.staminaWheelPos();
		if (staminaWheelPos <= 2) return;

		int wheels = (int)Math.ceil(staminaWheelPos);
		int color = wheelColor(wheels - 3);
		int wheelIndicatorColor = wheels == 3 ? 0 : color;

		if (this.outerWheelEmptyAnim.isActive()) {
			float d = Math.min(1, (float)this.outerWheelEmptyAnim.activeDuration() / OUTER_WHEEL_EMPTY_DURATION);
			color = lerpColor(d, wheelBgColor(wheels - 3), color);
			wheelIndicatorColor = lerpColor(d, wheelColor(wheels - 2),
					wheels == 3 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 3));
		}

		if (this.fullAnim.isActive()) {
			color = this.fullAnim.getGlowColor(color);
			if (wheels >= 4) wheelIndicatorColor = color;
		} else if (this.recoverAnim.isActive()) {
			wheelIndicatorColor = color = this.recoverAnim.getGlowColor(color);
		}

		wheel.fillWheel(2, staminaWheelPos, color);

		if (wheels >= 4) {
			int bgColor = wheelBgColor(wheels - 4);

			if (this.outerWheelFillAnim.isActive()) {
				float d = Math.min(1, (float)this.outerWheelFillAnim.activeDuration() / OUTER_WHEEL_FILL_DURATION);
				bgColor = lerpColor(d, wheelColor(wheels - 4), bgColor);
				wheelIndicatorColor = lerpColor(d,
						wheels == 4 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 4),
						wheelColor(wheels - 3));
			}

			if (this.fullAnim.isActive()) {
				bgColor = this.fullAnim.getGlowColor(bgColor);
			} else if (this.recoverAnim.isActive()) {
				bgColor = this.recoverAnim.getGlowColor(bgColor);
			}

			wheel.fillWheel(staminaWheelPos, (float)Math.ceil(staminaWheelPos), bgColor);
		}

		wheel.setIndicatorColor(wheelIndicatorColor);
	}

	public void reset() {
		this.fullAnim.setActive(FADE_END);

		this.outerWheelFillAnim.reset();
		this.outerWheelEmptyAnim.reset();
		this.recoverAnim.reset();
		this.gainExtraStaminaAnim.reset();

		this.prevDepleted = false;
		this.prevWheelIndex = -1;
		this.prevMaxStamina = Double.NaN;
		this.prevExtraStamina = 0;
	}
}
