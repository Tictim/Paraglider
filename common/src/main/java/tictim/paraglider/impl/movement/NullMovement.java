package tictim.paraglider.impl.movement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;

public final class NullMovement implements Movement{
	private NullMovement(){}

	public static final NullMovement instance = new NullMovement();

	@NotNull public static NullMovement get(){
		return instance;
	}

	@Override @NotNull public PlayerState state(){
		return ParagliderMod.instance().getPlayerStateMap().getIdleState();
	}

	@Override @Range(from = 0, to = Integer.MAX_VALUE) public int recoveryDelay(){
		return 0;
	}
	@Override public void setRecoveryDelay(int recoveryDelay){}

	@Override public double staminaReductionRate(){
		return 0;
	}
	@Override public int getActualStaminaDelta(){
		return 0;
	}
}
