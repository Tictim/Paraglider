package tictim.paraglider.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.client.render.StaminaWheelConstants.EXTRA;
import static tictim.paraglider.client.render.StaminaWheelConstants.wheelColor;

@NullMarked
public class SettingsWidgetStaminaWheelRenderer extends StaminaWheelRenderer {
	private int wheels = 1;
	private int extraWheels = 0;

	public int getWheels() {
		return wheels;
	}
	public void setWheels(int wheels) {
		this.wheels = Mth.clamp(wheels, 1, 3);
	}

	public int extraWheels() {
		return extraWheels;
	}
	public void setExtraWheels(int extraWheels) {
		this.extraWheels = extraWheels;
	}

	@Override protected void makeWheel(Player player, float partialTicks) {
		this.mainWheel.setProperties(this.wheels * Stamina.STAMINA_PER_WHEEL, this.wheels * Stamina.STAMINA_PER_WHEEL);
		this.mainWheel.fillWheel(0f, this.wheels, wheelColor(0));

		this.extraWheel.setProperties(this.extraWheels * Stamina.STAMINA_PER_WHEEL - 600, this.extraWheels * Stamina.STAMINA_PER_WHEEL);
		this.extraWheel.fillWheel(0f, this.extraWheels - 0.6f, EXTRA);
	}

	@Override protected boolean isDebugEnabled(Player player) {
		return false;
	}
}
