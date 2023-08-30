package tictim.paraglider.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.impl.vessel.SimpleVesselContainer;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

public class ClientPlayerMovement extends RemotePlayerMovement{
	private boolean wasParagliding;

	public ClientPlayerMovement(@NotNull LocalPlayer player){
		super(player);
	}

	@Override @NotNull public LocalPlayer player(){
		return (LocalPlayer)super.player();
	}
	@Override @NotNull protected Stamina createStamina(){
		return ParagliderAPI.staminaFactory().createLocalClientInstance(player());
	}
	@Override @NotNull protected VesselContainer createVesselContainer(){
		return new SimpleVesselContainer(player());
	}

	@Override public void update(){
		stamina().update(this);

		Player player = player();
		boolean paragliding = state().has(FLAG_PARAGLIDING);
		if(!player.isCreative()&&stamina().isDepleted()){
			player.setSprinting(false);
			player.setSwimming(false);
		}else if(wasParagliding!=paragliding){
			player.setSprinting(paragliding);
		}

		applyMovement();

		wasParagliding = paragliding;
	}
}
