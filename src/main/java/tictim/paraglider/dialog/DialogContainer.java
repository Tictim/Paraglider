package tictim.paraglider.dialog;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.vector.Vector3f;
import tictim.paraglider.ParagliderMod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DialogContainer extends Container{
	private final PlayerInventory playerInventory;
	private final Dialog dialog;

	private boolean hasLookAt = false;
	private final Vector3f lookAt = new Vector3f();

	private boolean cheated = false;

	public final Map<String, Object> storedFlags = new HashMap<>();

	private final IntArray trackedIntArray;

	public DialogContainer(ContainerType<?> containerType, int id, PlayerInventory playerInventory, Dialog dialog){
		super(containerType, id);
		this.playerInventory = playerInventory;
		this.dialog = Objects.requireNonNull(dialog);

		this.trackedIntArray = new IntArray(dialog.getTrackedInts().size());

		trackInt(new IntReferenceHolder(){
			@Override public int get(){
				return hasLookAt ? 1 : 0;
			}
			@Override public void set(int value){
				hasLookAt = value!=0;
			}
		});
		trackInt(new IntReferenceHolder(){
			@Override public int get(){
				return Float.floatToIntBits(lookAt.getX());
			}
			@Override public void set(int value){
				lookAt.setX(Float.intBitsToFloat(value));
			}
		});
		trackInt(new IntReferenceHolder(){
			@Override public int get(){
				return Float.floatToIntBits(lookAt.getY());
			}
			@Override public void set(int value){
				lookAt.setY(Float.intBitsToFloat(value));
			}
		});
		trackInt(new IntReferenceHolder(){
			@Override public int get(){
				return Float.floatToIntBits(lookAt.getZ());
			}
			@Override public void set(int value){
				lookAt.setZ(Float.intBitsToFloat(value));
			}
		});

		trackIntArray(trackedIntArray);
	}

	public Dialog getDialog(){
		return dialog;
	}

	public PlayerInventory getPlayerInventory(){
		return playerInventory;
	}

	public boolean isCheated(){
		return this.cheated;
	}
	public void setCheated(boolean cheated){
		this.cheated = cheated;
	}

	@Nullable public Vector3f getLookAt(){
		return hasLookAt ? lookAt : null;
	}
	public void setLookAt(@Nullable Vector3f lookAt){
		if(lookAt==null){
			this.hasLookAt = false;
			this.lookAt.set(0, 0, 0);
		}else{
			this.hasLookAt = true;
			this.lookAt.set(lookAt.getX(), lookAt.getY(), lookAt.getZ());
		}
	}

	@Override public void detectAndSendChanges(){
		List<IntTracker> trackedInts = dialog.getTrackedInts();
		for(int i = 0; i<trackedInts.size(); i++){
			this.trackedIntArray.set(i, trackedInts.get(i).getCount(playerInventory.player));
		}

		super.detectAndSendChanges();
	}

	@Override public boolean canInteractWith(PlayerEntity playerIn){
		return true;
	}

	@Override public void onContainerClosed(PlayerEntity playerIn){
		if(cheated)
			ParagliderMod.LOGGER.info(playerIn.getGameProfile().getName()+" cheated");
		dialog.onContainerClosed(this, playerIn);
	}
	public int getTrackedInt(int index){
		return trackedIntArray.get(index);
	}
}
