package tictim.paraglider.client;

import tictim.paraglider.capabilities.PlayerMovement;

import static tictim.paraglider.client.StaminaWheelConstants.*;

public class BargainScreenStaminaWheelRenderer extends StaminaWheelRenderer{
	private int internalStamina;
	private long lastUpdateTimestamp;
	private long timeSinceFull;
	private boolean gainedStamina;

	public BargainScreenStaminaWheelRenderer(int internalStamina){
		this.internalStamina = internalStamina;
		this.lastUpdateTimestamp = System.currentTimeMillis();
	}

	@Override protected void makeWheel(PlayerMovement h){
		int maxStamina = h.getMaxStamina();

		long newTimestamp = System.currentTimeMillis();
		long timePassed = newTimestamp-lastUpdateTimestamp;
		lastUpdateTimestamp = newTimestamp;

		if(internalStamina>maxStamina){
			internalStamina = Math.max(internalStamina-getStaminaChange(timePassed), maxStamina);
			timeSinceFull = 0;
			gainedStamina = false;
		}else if(internalStamina<maxStamina){
			internalStamina = Math.min(internalStamina+getStaminaChange(timePassed), maxStamina);
			timeSinceFull = 0;
			gainedStamina = true;
		}else{
			timeSinceFull += timePassed;
		}

		if(internalStamina>maxStamina){
			for(WheelLevel t : WheelLevel.values()){
				addWheel(t, 0, t.getProportion(maxStamina), IDLE);
				addWheel(t, t.getProportion(maxStamina), t.getProportion(internalStamina), EVIL_GLOW);
			}
		}else if(internalStamina<maxStamina){
			for(WheelLevel t : WheelLevel.values())
				addWheel(t, 0, t.getProportion(internalStamina), IDLE);
		}else if(gainedStamina&&timeSinceFull<GLOW_FADE){
			for(WheelLevel t : WheelLevel.values()){
				addWheel(t, 0, t.getProportion(maxStamina-PlayerMovement.STAMINA_INCREMENT), IDLE);
				addWheel(t, t.getProportion(maxStamina-PlayerMovement.STAMINA_INCREMENT), t.getProportion(maxStamina), GLOW.blend(IDLE, (float)(timeSinceFull)/GLOW_FADE));
			}
		}else{
			for(WheelLevel t : WheelLevel.values())
				addWheel(t, 0, t.getProportion(maxStamina), IDLE);
		}
	}

	private int getStaminaChange(long timePassed){
		return (int)(timePassed*(PlayerMovement.STAMINA_INCREMENT/500.0));
	}
}
