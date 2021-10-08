package tictim.paraglider.capabilities;

import net.minecraft.world.entity.player.Player;

public class ClientPlayerMovement extends RemotePlayerMovement{
	private PlayerState prevState = PlayerState.IDLE;

	public ClientPlayerMovement(Player player){
		super(player);
	}

	@Override public void update(){
		updateStamina();

		if(!player.isCreative()&&isDepleted()){
			player.setSprinting(false);
			player.setSwimming(false);
		}else if(prevState.isParagliding()!=getState().isParagliding()) player.setSprinting(getState().isParagliding());

		applyMovement();

		prevState = getState();
	}
}
