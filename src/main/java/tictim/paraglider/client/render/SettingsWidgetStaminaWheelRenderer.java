package tictim.paraglider.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.client.render.StaminaWheelConstants.wheelColor;

public class SettingsWidgetStaminaWheelRenderer extends StaminaWheelRenderer {
	private int wheels = 1;

	public int getWheels() {
		return wheels;
	}
	public void setWheels(int wheels) {
		this.wheels = Mth.clamp(wheels, 1, 3);
	}

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel, float partialTicks) {
		wheel.setProperties(this.wheels * Stamina.STAMINA_PER_WHEEL, this.wheels * Stamina.STAMINA_PER_WHEEL);
		wheel.fillWheel(0f, this.wheels, wheelColor(0));
	}

	@Override protected boolean isDebugEnabled(@NotNull Player player) {
		return false;
	}
}
