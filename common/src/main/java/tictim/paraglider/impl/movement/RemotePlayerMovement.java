package tictim.paraglider.impl.movement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.impl.vessel.NullVesselContainer;
import tictim.paraglider.network.SyncMovementHandle;

public class RemotePlayerMovement extends PlayerMovement implements SyncMovementHandle{
	public RemotePlayerMovement(@NotNull Player player){
		super(player);
	}

	@Override @NotNull protected Stamina createStamina(){
		return ParagliderAPI.staminaFactory().createRemoteInstance(player());
	}
	@Override @NotNull protected VesselContainer createVesselContainer(){
		return NullVesselContainer.get(); // n/a for remote players
	}

	@Override public void update(){}

	@Override public void syncMovement(@NotNull ResourceLocation stateId, int stamina, boolean depleted, int recoveryDelay){
		PlayerStateMap stateMap = ParagliderMod.instance().getPlayerStateMap();
		PlayerState state = stateMap.getState(stateId);
		setState(state==null ? stateMap.getIdleState() : state);

		stamina().setStamina(stamina);
		stamina().setDepleted(depleted);

		setRecoveryDelay(recoveryDelay);
	}
}
