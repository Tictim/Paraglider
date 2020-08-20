package tictim.paraglider.capabilities;

import javax.annotation.Nullable;
import java.util.Objects;

public enum PlayerState{
	IDLE(ParaglidingAction.NONE, StaminaAction.RECOVER),
	MIDAIR(ParaglidingAction.NONE, StaminaAction.NO_CHANGE),
	RUNNING(ParaglidingAction.NONE, StaminaAction.FASTEST_CONSUME),
	SWIMMING(ParaglidingAction.NONE, StaminaAction.FASTER_CONSUME),
	UNDERWATER(ParaglidingAction.NONE, StaminaAction.SLOW_CONSUME),
	BREATHING_UNDERWATER(ParaglidingAction.NONE, StaminaAction.SLOW_RECOVER),
	PARAGLIDING(ParaglidingAction.PARAGLIDING, StaminaAction.SLOW_CONSUME),
	ASCENDING(ParaglidingAction.ASCENDING, StaminaAction.SLOW_CONSUME);

	public final ParaglidingAction paraglidingAction;
	public final StaminaAction staminaAction;
	@Nullable public final PlayerState optionalFallbackState;

	PlayerState(ParaglidingAction paraglidingAction, StaminaAction staminaAction){
		this(paraglidingAction, staminaAction, null);
	}
	PlayerState(ParaglidingAction paraglidingAction, StaminaAction staminaAction, @Nullable PlayerState optionalFallbackState){
		this.paraglidingAction = Objects.requireNonNull(paraglidingAction);
		this.staminaAction = Objects.requireNonNull(staminaAction);
		this.optionalFallbackState = optionalFallbackState;
	}

	public boolean isParagliding(){
		return paraglidingAction.isParagliding();
	}

	public static PlayerState of(int meta){
		PlayerState[] values = values();
		return values[meta%values.length];
	}

	public enum ParaglidingAction{
		NONE,
		PARAGLIDING,
		ASCENDING;

		public boolean isParagliding(){
			return this!=NONE;
		}
	}

	public enum StaminaAction{
		NO_CHANGE(0, false),
		FASTEST_CONSUME(10, true),
		FASTER_CONSUME(6, true),
		SLOW_CONSUME(3, true),
		SLOW_RECOVER(10, false),
		RECOVER(20, false),
		FAST_RECOVER(50, false);

		public final int change;
		public final boolean isConsume;

		StaminaAction(int change, boolean isConsume){
			this.change = change;
			this.isConsume = isConsume;
		}
	}
}
