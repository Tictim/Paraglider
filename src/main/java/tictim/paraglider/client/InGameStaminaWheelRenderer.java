package tictim.paraglider.client;

import net.minecraft.util.FastColor.ARGB32;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.PlayerState;

import static tictim.paraglider.client.StaminaWheelConstants.*;

public class InGameStaminaWheelRenderer extends StaminaWheelRenderer{
	private int prevStamina;
	private long fullTime;

	@Override protected void makeWheel(PlayerMovement h){
		int stamina = h.getStamina();
		int maxStamina = h.getMaxStamina();
		if(stamina>=maxStamina){
			long time = System.currentTimeMillis();
			long timeDiff;
			if(prevStamina!=stamina){
				prevStamina = stamina;
				fullTime = time;
				timeDiff = 0;
			}else timeDiff = time-fullTime;
			int color = StaminaWheelConstants.getGlowAndFadeColor(timeDiff);
			if(ARGB32.alpha(color)<=0) return;
			for(WheelLevel t : WheelLevel.values())
				addWheel(t, 0, t.getProportion(stamina), color);
		}else{
			prevStamina = stamina;
			int color = ARGB32.lerp(cycle(System.currentTimeMillis(), h.isDepleted() ? DEPLETED_BLINK : BLINK), DEPLETED_1, DEPLETED_2);
			PlayerState state = h.getState();
			for(WheelLevel t : WheelLevel.values()){
				addWheel(t, 0, t.getProportion(maxStamina), EMPTY);
				if(h.isDepleted()){
					addWheel(t, 0, t.getProportion(stamina), color);
				}else{
					addWheel(t, 0, t.getProportion(stamina), IDLE);
					if(state.isConsume()&&(state.isParagliding() ? ModCfg.paraglidingConsumesStamina() : ModCfg.runningConsumesStamina())){
						addWheel(t, t.getProportion(stamina+state.change()*10), t.getProportion(stamina), color);
					}
				}
			}
		}
	}
}
