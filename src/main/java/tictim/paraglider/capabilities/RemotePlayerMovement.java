package tictim.paraglider.capabilities;

import net.minecraft.world.entity.player.Player;

/**
 * PlayerMovement with no unique action at update. Used as simple data container.
 */
public class RemotePlayerMovement extends PlayerMovement{
	private boolean paragliding;

	public RemotePlayerMovement(Player player){
		super(player);
	}

	@Override public boolean isParagliding(){
		return paragliding;
	}
	@Override public void update(){}
	public void setParagliding(boolean paragliding){
		this.paragliding = paragliding;
	}
}
