package tictim.paraglider.impl.movement;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.network.SyncMovementHandle;

@NullMarked
public class RemotePlayerMovement extends PlayerMovement implements SyncMovementHandle {
	private int recoveryDelay;
	private double staminaEfficiency;

	public RemotePlayerMovement(Player player) {
		super(player);
	}

	@Override protected Stamina createCustomStamina() {
		return ParagliderAPI.staminaFactory().createRemoteInstance(player());
	}

	@Override protected boolean isRemote() {
		return true;
	}

	@Override public void update() {}

	@Override public void syncMovement(Identifier stateId, int recoveryDelay, double efficiency) {
		PlayerStateMap stateMap = ParagliderMod.instance().getPlayerStateMap();
		PlayerState state = stateMap.getState(stateId);
		setState(state == null ? stateMap.getIdleState() : state);
		setRecoveryDelay(recoveryDelay);
		this.staminaEfficiency = efficiency;
	}

	@Range(from = 0, to = Integer.MAX_VALUE)
	@Override public int recoveryDelay() {
		return recoveryDelay;
	}
	@Override public void setRecoveryDelay(int recoveryDelay) {
		this.recoveryDelay = Math.max(0, recoveryDelay);
	}
	@Override public double staminaDelta() {
		return StaminaEfficiencyLogic.applyEfficiency(state().staminaDelta(), staminaEfficiency());
	}

	public double staminaEfficiency() {
		return this.staminaEfficiency;
	}
}
