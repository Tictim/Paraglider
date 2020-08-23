package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import tictim.paraglider.utils.ParagliderUtils;

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

	protected void updateParagliderInInventory(){
		boolean isParagliding = isParagliding();
		for(int i = 0; i<player.inventory.getSizeInventory(); i++){
			Paraglider cap = player.inventory.getStackInSlot(i).getCapability(Paraglider.CAP).orElse(null);
			if(cap!=null){
				if(i==player.inventory.currentItem){
					if(cap.isParagliding!=isParagliding){
						cap.isParagliding = isParagliding;
						ParagliderUtils.resetMainHandItemEquipProgress();
					}
				}else cap.isParagliding = false;
			}
		}
	}
}
