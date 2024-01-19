package tictim.paraglider.impl.stamina;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.Copy;
import tictim.paraglider.api.Serde;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

/**
 * Implementation of in-house stamina system modeled after BotW's stamina feature.
 */
public class BotWStamina implements Stamina, Copy, Serde{
	private final VesselContainer vessels;
	private int stamina;
	private boolean depleted;

	public BotWStamina(@NotNull VesselContainer vessels){
		this.vessels = vessels;
		this.stamina = maxStamina();
	}

	@Override public int stamina(){
		return stamina;
	}
	@Override public void setStamina(int stamina){
		this.stamina = stamina;
	}
	@Override public int maxStamina(){
		return Cfg.get().maxStamina(vessels.staminaVessel());
	}
	@Override public boolean isDepleted(){
		return depleted;
	}
	@Override public void setDepleted(boolean depleted){
		this.depleted = depleted;
	}

	@Override public void update(@NotNull Movement movement){
		PlayerState state = movement.state();
		int recoveryDelay = movement.recoveryDelay();
		int newRecoveryDelay = recoveryDelay;
		int delta = movement.getActualStaminaDelta();
		if(delta<0){
			if(!isDepleted()) takeStamina(-delta, false, false);
		}else{
			if(recoveryDelay>0) newRecoveryDelay--;
			else if(delta>0) giveStamina(delta, false);
		}
		//noinspection DataFlowIssue
		newRecoveryDelay = Math.max(0, Math.max(newRecoveryDelay, state.recoveryDelay()));
		if(recoveryDelay!=newRecoveryDelay) movement.setRecoveryDelay(newRecoveryDelay);
	}

	@Override public int giveStamina(int amount, boolean simulate){
		if(amount<=0) return 0;
		int staminaToGive = Math.min(amount, maxStamina()-this.stamina);
		if(staminaToGive<=0) return 0;
		if(!simulate) this.stamina += staminaToGive;
		return staminaToGive;
	}

	@Override public int takeStamina(int amount, boolean simulate, boolean ignoreDepletion){
		if(amount<=0||(isDepleted()&&!ignoreDepletion)) return 0;
		int staminaToTake = Math.min(amount, this.stamina);
		if(staminaToTake<=0) return 0;
		if(!simulate) this.stamina -= staminaToTake;
		return staminaToTake;
	}

	@Override public void copyFrom(@NotNull Object from){
		if(!(from instanceof Stamina stamina)) return;
		this.stamina = stamina.stamina();
		this.depleted = stamina.isDepleted();
	}

	@Override public void read(@NotNull CompoundTag tag){
		this.stamina = tag.getInt("stamina");
		this.depleted = tag.getBoolean("depleted");
	}

	@Override @NotNull public CompoundTag write(){
		CompoundTag tag = new CompoundTag();
		tag.putInt("stamina", stamina);
		tag.putBoolean("depleted", depleted);
		return tag;
	}
}
