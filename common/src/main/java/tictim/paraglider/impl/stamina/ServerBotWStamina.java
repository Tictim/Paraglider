package tictim.paraglider.impl.stamina;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

public class ServerBotWStamina extends BotWStamina{
	public ServerBotWStamina(@NotNull VesselContainer vessels){
		super(vessels);
	}

	@Override public void update(@NotNull Movement movement){
		boolean wasDepleted = isDepleted();
		super.update(movement);
		if(isDepleted()){
			if(stamina()>=maxStamina()){
				setDepleted(false);
				if(movement instanceof ServerPlayerMovement spm) spm.markMovementChanged();
			}
		}else if(stamina()<=0){
			setDepleted(true);
			if(movement instanceof ServerPlayerMovement spm){
				spm.resetPanicParaglidingState();
				spm.markMovementChanged();
			}
		}
		if(wasDepleted!=isDepleted()&&movement instanceof ServerPlayerMovement spm) spm.markMovementChanged();
	}
}
