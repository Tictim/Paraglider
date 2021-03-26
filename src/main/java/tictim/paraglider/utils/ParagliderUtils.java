package tictim.paraglider.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ParagliderUtils{
	private ParagliderUtils(){}

	public static void resetMainHandItemEquipProgress(){
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Client::resetMainHandItemEquipProgress);
	}

	public static int count(PlayerEntity player, Item item){
		return count(player.inventory, item);
	}
	public static int count(IInventory inventory, Item item){
		int count = 0;
		for(int i = 0; i<inventory.getSizeInventory(); i++){
			ItemStack s = inventory.getStackInSlot(i);
			if(!s.isEmpty()&&s.getItem()==item) count += s.getCount();
		}
		return count;
	}

	public static boolean consume(PlayerEntity player, Item item, int count){
		return consume(player.inventory, item, count);
	}
	public static boolean consume(IInventory inventory, Item item, int count){
		return tryConsume(inventory, item, count, false);
	}

	public static boolean canConsume(PlayerEntity player, Item item, int count){
		return canConsume(player.inventory, item, count);
	}
	public static boolean canConsume(IInventory inventory, Item item, int count){
		return tryConsume(inventory, item, count, true);
	}

	public static boolean tryConsume(IInventory inventory, Item item, int count, boolean simulate){
		int c = 0;
		for(int i = 0; i<inventory.getSizeInventory(); i++){
			ItemStack s = inventory.getStackInSlot(i);
			if(!s.isEmpty()&&s.getItem()==item){
				c += s.getCount();
				if(c>=count){
					if(simulate) return true;
					for(; i>=0; i--){
						ItemStack s2 = inventory.getStackInSlot(i);
						if(!s2.isEmpty()&&s2.getItem()==item){
							if(s2.getCount()>count){
								s2.shrink(count);
								count = 0;
							}else{
								inventory.setInventorySlotContents(i, ItemStack.EMPTY);
								count -= s2.getCount();
							}
							if(count<=0) return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	public static final class Client{
		private Client(){}

		public static void resetMainHandItemEquipProgress(){
			Minecraft.getInstance().gameRenderer.itemRenderer.resetEquippedProgress(Hand.MAIN_HAND);
		}
	}
}
