package tictim.paraglider.recipe.bargain;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.StatueDialogMsg;
import tictim.paraglider.network.SyncLookAtMsg;
import tictim.paraglider.network.UpdateBargainPreviewMsg;
import tictim.paraglider.utils.StatueDialog;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StatueBargainContainer extends Container{
	private final PlayerInventory playerInventory;

	private final List<StatueBargain> bargains;
	@Nullable private final StatueDialog dialog;

	private int heartContainerCache;
	private int staminaVesselCache;
	private int essenceCache;
	private final Preview[] previousBargainTest;
	private final NonNullList<ItemStack> inventoryCache;

	@Nullable private Vector3d lookAt, prevLookAt;

	private boolean redoBargainTest = true;
	private boolean sendInitMessage = true;

	public StatueBargainContainer(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory){
		this(type, id, playerInventory, null);
	}
	public StatueBargainContainer(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory, @Nullable StatueDialog dialog){
		super(type, id);

		this.bargains = playerInventory.player.world.getRecipeManager()
				.getRecipesForType(Contents.STATUE_BARGAIN_RECIPE_TYPE)
				.stream()
				.filter(b -> type==null||b.getBargainOwner().equals(type.getRegistryName()))
				.sorted(Comparator.comparing(IRecipe::getId))
				.collect(Collectors.toList());
		this.dialog = dialog;

		this.playerInventory = playerInventory;

		this.previousBargainTest = new Preview[bargains.size()];
		for(int i = 0; i<previousBargainTest.length; i++)
			this.previousBargainTest[i] = new Preview();
		this.inventoryCache = NonNullList.withSize(playerInventory.getSizeInventory(), ItemStack.EMPTY);
		for(int i = 0; i<playerInventory.getSizeInventory(); i++){
			ItemStack stack = playerInventory.getStackInSlot(i);
			if(!stack.isEmpty()) inventoryCache.set(i, stack.copy());
		}
	}

	public List<StatueBargain> getBargains(){
		return bargains;
	}

	@Nullable
	public StatueBargain getBargain(int bargainIndex){
		if(bargainIndex<0) return null;
		return bargains.size()>bargainIndex ? bargains.get(bargainIndex) : null;
	}

	public boolean canBargain(int bargainIndex){
		if(bargainIndex<0) return false;
		return previousBargainTest.length>bargainIndex&&previousBargainTest[bargainIndex].canBargain;
	}
	public ItemDemand[] getDemandPreview(int bargainIndex){
		if(bargainIndex<0) return new ItemDemand[0];
		return previousBargainTest.length>bargainIndex ? previousBargainTest[bargainIndex].demands : new ItemDemand[0];
	}

	@Nullable public Vector3d getLookAt(){
		return lookAt;
	}

	public void setLookAt(@Nullable Vector3d lookAt){
		this.lookAt = lookAt;
	}

	@Override public boolean canInteractWith(PlayerEntity playerIn){
		return true;
	}

	@Override public void detectAndSendChanges(){
		ServerPlayerMovement m = ServerPlayerMovement.of(playerInventory.player);
		if(m!=null){
			if(heartContainerCache!=m.getHeartContainers()){
				redoBargainTest = true;
				heartContainerCache = m.getHeartContainers();
			}
			if(staminaVesselCache!=m.getStaminaVessels()){
				redoBargainTest = true;
				staminaVesselCache = m.getStaminaVessels();
			}
			if(essenceCache!=m.getEssence()){
				redoBargainTest = true;
				essenceCache = m.getEssence();
			}
		}
		if(inventoryChanged()) redoBargainTest = true;
		if(redoBargainTest) updateBargainTest();
		if(sendInitMessage){
			sendInitMessage = false;
			sendDialog(StatueDialog.Case.INITIAL, null, null);
		}
		if(prevLookAt!=lookAt){
			prevLookAt = lookAt;
			sendToPlayer(new SyncLookAtMsg(lookAt));
		}
		super.detectAndSendChanges();
	}

	private boolean inventoryChanged(){
		boolean changed = false;
		for(int i = 0; i<playerInventory.getSizeInventory(); ++i){
			ItemStack s1 = this.playerInventory.getStackInSlot(i);
			ItemStack s2 = this.inventoryCache.get(i);
			if(!ItemStack.areItemStacksEqual(s2, s1)){
				ItemStack copy = s1.copy();
				this.inventoryCache.set(i, copy);
				changed = true;
			}
		}
		return changed;
	}

	private void updateBargainTest(){
		UpdateBargainPreviewMsg msg = null;
		for(int i = 0; i<bargains.size(); i++){
			Preview preview = previousBargainTest[i];
			StatueBargain bargain = bargains.get(i);

			boolean canBargainUpdated = preview.updateCanBargain(bargain, playerInventory.player);
			boolean demandPreviewUpdated = preview.updateDemandPreview(bargain, playerInventory.player);

			if(canBargainUpdated||demandPreviewUpdated){
				if(msg==null) msg = new UpdateBargainPreviewMsg();
				msg.add(bargain.getId(), preview, demandPreviewUpdated);
			}
		}
		if(msg!=null) sendToPlayer(msg);
		redoBargainTest = false;
	}

	public void sendDialog(StatueBargain bargain, BargainResult result){
		sendDialog(result.isSuccess() ? StatueDialog.Case.BARGAIN_SUCCESS : StatueDialog.Case.BARGAIN_FAILURE, bargain, result);
	}

	private void sendDialog(StatueDialog.Case dialogCase, @Nullable StatueBargain bargain, @Nullable BargainResult result){
		if(this.dialog==null) return;
		ITextComponent dialog = this.dialog.getDialog(playerInventory.player.getRNG(), dialogCase, bargain, result);
		if(dialog!=null) sendToPlayer(new StatueDialogMsg(dialog));
	}

	private void sendToPlayer(Object message){
		PlayerEntity player = playerInventory.player;
		if(player instanceof ServerPlayerEntity)
			ModNet.NET.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), message);
	}

	/**
	 * Client only method. Don't use it. I'll cut your throat.
	 *
	 * @param key          ID of the bargain recipe
	 * @param booleanValue The new canBargain value
	 */
	public void setCanBargain(ResourceLocation key, boolean booleanValue){
		List<StatueBargain> bargains = getBargains();
		for(int i = 0, bargainsSize = bargains.size(); i<bargainsSize; i++){
			StatueBargain bargain = bargains.get(i);
			if(key.equals(bargain.getId())){
				previousBargainTest[i].setCanBargain(booleanValue);
				return;
			}
		}
	}
	public void setDemandPreview(ResourceLocation key, ItemDemand[] demands){
		List<StatueBargain> bargains = getBargains();
		for(int i = 0, bargainsSize = bargains.size(); i<bargainsSize; i++){
			StatueBargain bargain = bargains.get(i);
			if(key.equals(bargain.getId())){
				previousBargainTest[i].setDemands(demands);
				return;
			}
		}
	}

	public static final class Preview{
		private boolean canBargain;
		private ItemDemand[] demands = new ItemDemand[0];

		public boolean canBargain(){
			return canBargain;
		}
		public void setCanBargain(boolean canBargain){
			this.canBargain = canBargain;
		}
		public ItemDemand[] getDemands(){
			return demands;
		}
		public void setDemands(ItemDemand[] demands){
			this.demands = demands;
		}

		public boolean updateCanBargain(StatueBargain bargain, PlayerEntity player){
			boolean canBargain = bargain.bargain(player, true).isSuccess();
			if(this.canBargain==canBargain) return false;
			this.canBargain = canBargain;
			return true;
		}

		public boolean updateDemandPreview(StatueBargain bargain, PlayerEntity player){
			ItemDemand[] demands = bargain
					.getPreview()
					.getDemands()
					.stream()
					.map(demand -> new ItemDemand(
							demand.getPreviewItems(),
							demand.getQuantity(),
							demand.getCounter().count(player)))
					.toArray(ItemDemand[]::new);
			if(isSame(this.demands, demands)) return false;
			this.demands = demands;
			return true;
		}

		private static boolean isSame(ItemDemand[] a1, ItemDemand[] a2){
			if(a1.length!=a2.length) return false;
			for(int i = 0; i<a1.length; i++)
				if(a1[i].count!=a2[i].count) return false;
			return true;
		}
	}

	public static final class ItemDemand{
		private final ItemStack[] previewItems;
		private final int quantity;
		private final int count;

		public ItemDemand(ItemStack[] previewItems, int quantity, int count){
			this.previewItems = previewItems;
			this.quantity = quantity;
			this.count = count;
		}

		public ItemStack[] getPreviewItems(){
			return previewItems;
		}
		public int getQuantity(){
			return quantity;
		}
		public int getCount(){
			return count;
		}
	}
}
