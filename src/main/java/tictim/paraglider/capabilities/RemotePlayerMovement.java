package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;

/**
 * PlayerMovement with no unique action at update. Used as simple data container.
 */
public class RemotePlayerMovement extends PlayerMovement{
	private boolean paragliding;

	public RemotePlayerMovement(PlayerEntity player){
		super(player);
	}

	@Override public boolean isParagliding(){
		return paragliding;
	}
	public void setParagliding(boolean paragliding){
		this.paragliding = paragliding;
	}

	@Override public void update(){
		updateParagliderInInventory();
	}
}
