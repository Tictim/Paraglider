package tictim.paraglider.client.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class SettingsWidgetStaminaWheelRenderer extends StaminaWheelRenderer{
	private int wheels = 1;

	public int getWheels(){
		return wheels;
	}
	public void setWheels(int wheels){
		this.wheels = Mth.clamp(wheels, 1, 3);
	}

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel){
		wheel.fill(0, WheelLevel.values()[wheels-1].end(), StaminaWheelConstants.IDLE);
	}
}
