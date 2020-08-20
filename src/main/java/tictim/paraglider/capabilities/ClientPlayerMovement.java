package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;

public class ClientPlayerMovement extends RemotePlayerMovement{
	private PlayerState prevState = PlayerState.IDLE;

	public ClientPlayerMovement(PlayerEntity player){
		super(player);
	}

	@Override public void update(){
		updateStamina();

		if(!player.abilities.isCreativeMode&&isDepleted()){
			player.setSprinting(false);
			player.setSwimming(false);
		}else if(prevState.isParagliding()!=getState().isParagliding()) player.setSprinting(getState().isParagliding());

		applyMovement();
		updateParagliderInInventory();

		prevState = getState();
	}
}
