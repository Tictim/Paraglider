package tictim.paraglider.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.client.render.StaminaWheelAnimationTracker.UpdateMode.*;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public class BargainScreenStaminaWheelRenderer extends StaminaWheelRenderer {
	private final StaminaWheelAnimationTracker fullAnim = new StaminaWheelAnimationTracker();
	private final StaminaWheelAnimationTracker outerWheelFillAnim = new StaminaWheelAnimationTracker(OUTER_WHEEL_FILL_DURATION);
	private final StaminaWheelAnimationTracker outerWheelEmptyAnim = new StaminaWheelAnimationTracker(OUTER_WHEEL_EMPTY_DURATION);

	private double stamina;
	private double maxStamina, prevMaxStamina;
	private int prevWheelIndex = -1;
	private boolean gainedStamina;
	private boolean full;

	public BargainScreenStaminaWheelRenderer() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		this.stamina = this.maxStamina = this.prevMaxStamina = Stamina.get(player).maxStamina();
	}

	public void tick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		double maxStamina = Stamina.get(player).maxStamina();
		if (maxStamina != this.maxStamina) {
			// only update prevMaxStamina if stamina value has caught up on max stamina
			if (this.maxStamina == this.stamina) {
				this.prevMaxStamina = this.maxStamina;
			}
			this.maxStamina = maxStamina;
		}

		if (this.stamina > maxStamina) {
			this.stamina = Math.max(this.stamina - 20, maxStamina);
			this.gainedStamina = false;
		} else if (this.stamina < maxStamina) {
			this.stamina = Math.min(this.stamina + 20, maxStamina);
			this.gainedStamina = true;
		}

		this.full = this.gainedStamina && this.stamina >= maxStamina;
	}

	@Override protected void makeWheel(@NotNull Player player, float partialTicks) {
		this.mainWheel.setProperties(this.stamina, Math.max(this.stamina, this.maxStamina));

		int wheelIndex = (int)Math.ceil(this.mainWheel.staminaWheelPos());

		this.fullAnim.update(this.full);
		this.outerWheelFillAnim.update(this.full ? SET_INACTIVE : this.prevWheelIndex < wheelIndex ? SET_ACTIVE : RETAIN);
		this.outerWheelEmptyAnim.update(this.full ? SET_INACTIVE : this.prevWheelIndex > wheelIndex ? SET_ACTIVE : RETAIN);

		this.prevWheelIndex = wheelIndex;

		this.mainWheel.fillStamina(0, Math.min(this.maxStamina, this.stamina), wheelColor(0));

		if (this.stamina > this.maxStamina) {
			this.mainWheel.fillStamina(this.maxStamina, this.stamina, EVIL_GLOW);
		} else if (this.full) {
			this.mainWheel.fillStamina(this.prevMaxStamina, this.maxStamina, this.fullAnim.getGlowColor(wheelColor(0)));
		}

		makeOuterWheel(this.mainWheel); // idk?

		debugAnim("full", this.fullAnim);
		debugAnim("outerWheelFill", this.outerWheelFillAnim);
		debugAnim("outerWheelEmpty", this.outerWheelEmptyAnim);
	}

	private void makeOuterWheel(Wheel wheel) {
		float staminaWheelPos = wheel.staminaWheelPos();
		if (staminaWheelPos <= 2) return;

		int wheels = (int)Math.ceil(staminaWheelPos);
		int color = wheelColor(wheels - 3);
		int wheelIndicatorColor = wheels == 3 ? 0 : color;

		if (this.outerWheelEmptyAnim.isActive()) {
			float d = Math.min(1, (float)this.outerWheelEmptyAnim.activeDuration() / OUTER_WHEEL_EMPTY_DURATION);
			color = ARGB.lerp(d, wheelBgColor(wheels - 3), color);
			wheelIndicatorColor = ARGB.lerp(d, wheelColor(wheels - 2),
					wheels == 3 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 3));
		}

		wheel.fillWheel(2, staminaWheelPos, color);

		if (this.stamina > this.maxStamina) {
			wheel.fillWheel(
					toWheelPos(this.maxStamina),
					Math.min(toWheelPos(this.stamina), staminaWheelPos),
					EVIL_GLOW);
		} else if (this.fullAnim.isActive()) {
			color = this.fullAnim.getGlowColor(color);
			if (wheels >= 4) wheelIndicatorColor = color;
			wheel.fillWheel(
					Math.max(toWheelPos(this.prevMaxStamina), wheels - 1),
					toWheelPos(this.maxStamina),
					color);
		}

		if (wheels >= 4) {
			int bgColor = wheelBgColor(wheels - 4);

			if (this.outerWheelFillAnim.isActive()) {
				float d = Math.min(1, (float)this.outerWheelFillAnim.activeDuration() / OUTER_WHEEL_FILL_DURATION);
				bgColor = ARGB.lerp(d, wheelColor(wheels - 4), bgColor);
				wheelIndicatorColor = ARGB.lerp(d,
						wheels == 4 ? ARGB.color(0, wheelColor(1)) : wheelColor(wheels - 4),
						wheelColor(wheels - 3));
			}

			wheel.fillWheel(staminaWheelPos, (float)Math.ceil(staminaWheelPos), bgColor);

			if (this.stamina > this.maxStamina) {
				wheel.fillWheel(toWheelPos(this.maxStamina) + 1, (float)Math.ceil(staminaWheelPos), EVIL_GLOW);
			} else if (this.fullAnim.isActive()) {
				wheel.fillWheel(
						toWheelPos(this.prevMaxStamina) + 1,
						Math.min(toWheelPos(this.maxStamina) + 1, (float)Math.ceil(staminaWheelPos)),
						this.fullAnim.getGlowColor(bgColor));
			}
		}

		wheel.setExtraWheelIndicatorColor(wheelIndicatorColor);
	}
}
