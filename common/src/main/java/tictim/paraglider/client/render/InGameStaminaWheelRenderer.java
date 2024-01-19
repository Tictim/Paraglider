package tictim.paraglider.client.render;

import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public class InGameStaminaWheelRenderer extends StaminaWheelRenderer{
	private static final InGameStaminaWheelRenderer instance = new InGameStaminaWheelRenderer();

	public static InGameStaminaWheelRenderer get(){
		return instance;
	}

	private boolean full = true;
	private long fullDuration = FADE_END;
	private long prevFullTime;

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel){
		Stamina s = Stamina.get(player);
		int maxStamina = s.maxStamina();
		int stamina = Math.min(maxStamina, s.stamina());

		if(stamina>=maxStamina){
			makeFullWheel(wheel, stamina);
			return;
		}

		this.full = false;
		boolean depleted = s.isDepleted();
		Movement movement = Movement.get(player);
		int staminaDelta = movement.getActualStaminaDelta();

		wheel.fill(0, maxStamina, EMPTY);
		if(depleted){
			wheel.fill(0, stamina, getBlinkColor(ms(), true));
		}else{
			wheel.fill(0, stamina, IDLE);
			if(staminaDelta<0){
				wheel.fill(stamina+staminaDelta*10, stamina, getBlinkColor(ms(), false));
			}
		}
	}

	private void makeFullWheel(@NotNull Wheel wheel, int stamina){
		long time = ms();
		long timeDiff;
		if(!this.full){
			this.full = true;
			this.fullDuration = timeDiff = 0;
		}else if(this.fullDuration<FADE_END){
			timeDiff = time-this.prevFullTime;
			this.fullDuration = Math.min(this.fullDuration+timeDiff, FADE_END);
		}else return;

		int color = getGlowAndFadeColor(timeDiff);
		if(ARGB32.alpha(color)<=0) return;
		wheel.fill(0, stamina, color);

		this.prevFullTime = time;
	}

	public void reset(){
		this.full = true;
		this.fullDuration = FADE_END;
		this.prevFullTime = 0;
	}
}
