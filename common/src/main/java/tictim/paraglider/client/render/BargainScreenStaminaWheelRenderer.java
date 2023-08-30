package tictim.paraglider.client.render;

import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public class BargainScreenStaminaWheelRenderer extends StaminaWheelRenderer{
	private int internalStamina;
	private long lastUpdateTimestamp;
	private long timeSinceFull;
	private boolean gainedStamina;

	private boolean initialized;

	@Override protected void makeWheel(@NotNull Player player, @NotNull Wheel wheel){
		VesselContainer vessels = VesselContainer.get(player);
		int maxStamina = Cfg.get().maxStamina(vessels.staminaVessel());

		if(!this.initialized){
			this.initialized = true;
			this.internalStamina = maxStamina;
			this.lastUpdateTimestamp = ms();
		}else{
			long newTimestamp = ms();
			long timePassed = newTimestamp-lastUpdateTimestamp;
			this.lastUpdateTimestamp = newTimestamp;

			if(internalStamina>maxStamina){
				this.internalStamina = Math.max(internalStamina-getStaminaChange(timePassed), maxStamina);
				this.timeSinceFull = 0;
				this.gainedStamina = false;
			}else if(internalStamina<maxStamina){
				this.internalStamina = Math.min(internalStamina+getStaminaChange(timePassed), maxStamina);
				this.timeSinceFull = 0;
				this.gainedStamina = true;
			}else if(timeSinceFull<GLOW_FADE){
				this.timeSinceFull += timePassed;
			}
		}

		if(internalStamina>maxStamina){
			wheel.fill(0, maxStamina, IDLE);
			wheel.fill(maxStamina, internalStamina, EVIL_GLOW);
		}else if(internalStamina<maxStamina){
			wheel.fill(0, internalStamina, IDLE);
		}else if(gainedStamina&&timeSinceFull<GLOW_FADE){
			int stamina = Cfg.get().maxStamina(vessels.staminaVessel()-1);
			wheel.fill(0, stamina, IDLE);
			wheel.fill(stamina, maxStamina, ARGB32.lerp((float)timeSinceFull/GLOW_FADE, GLOW, IDLE));
		}else{
			wheel.fill(0, maxStamina, IDLE);
		}
	}

	private int getStaminaChange(long timePassed){
		return (int)(timePassed*(0.4));
	}
}
