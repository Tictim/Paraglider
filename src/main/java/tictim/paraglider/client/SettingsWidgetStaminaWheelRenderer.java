package tictim.paraglider.client;

import net.minecraft.util.Mth;
import tictim.paraglider.capabilities.PlayerMovement;

public class SettingsWidgetStaminaWheelRenderer extends StaminaWheelRenderer{
	private int wheels = 1;

	public int getWheels(){
		return wheels;
	}
	public void setWheels(int wheels){
		this.wheels = Mth.clamp(wheels, 1, 3);
	}

	@Override protected void makeWheel(PlayerMovement h){
		WheelLevel[] values = WheelLevel.values();
		for(int i = 0; i<wheels; i++) addWheel(values[i], 0, 1, StaminaWheelConstants.IDLE);
	}
}
