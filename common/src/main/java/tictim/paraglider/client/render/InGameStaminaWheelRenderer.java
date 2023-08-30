package tictim.paraglider.client.render;

import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public class InGameStaminaWheelRenderer extends StaminaWheelRenderer{
	private boolean full;
	private long fullTime;

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel){
		Stamina s = Stamina.get(player);
		int maxStamina = s.maxStamina();
		int stamina = Math.min(maxStamina, s.stamina());
		if(stamina>=maxStamina){
			long time = ms();
			long timeDiff;
			if(!full){
				full = true;
				fullTime = time;
				timeDiff = 0;
			}else timeDiff = time-fullTime;
			int color = StaminaWheelConstants.getGlowAndFadeColor(timeDiff);
			if(ARGB32.alpha(color)<=0) return;
			wheel.fill(0, stamina, color);
		}else{
			full = false;
			boolean depleted = s.isDepleted();
			int color = ARGB32.lerp(cycle(ms(), depleted ? DEPLETED_BLINK : BLINK), DEPLETED_1, DEPLETED_2);
			Movement movement = Movement.get(player);
			PlayerState state = movement.state();

			wheel.fill(0, maxStamina, EMPTY);
			if(depleted){
				wheel.fill(0, stamina, color);
			}else{
				wheel.fill(0, stamina, IDLE);
				if(state.staminaDelta()<0){
					wheel.fill(stamina+state.staminaDelta()*10, stamina, color);
				}
			}
		}
	}
}
