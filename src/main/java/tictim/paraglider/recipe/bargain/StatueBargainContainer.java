package tictim.paraglider.recipe.bargain;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
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

public class StatueBargainContainer extends AbstractContainerMenu{
	private final Inventory playerInventory;

	private final List<StatueBargain> bargains;
	@Nullable private final StatueDialog dialog;

	@Nullable private ResourceLocation advancement;

	private int heartContainerCache;
	private int staminaVesselCache;
	private int essenceCache;
	private final Preview[] previousBargainTest;
	private final NonNullList<ItemStack> inventoryCache;

	@Nullable private Vec3 lookAt, prevLookAt;

	private boolean redoBargainTest = true;
	private boolean sendInitMessage = true;

	public StatueBargainContainer(@Nullable MenuType<?> type, int id, Inventory playerInventory){
		this(type, id, playerInventory, null, null);
	}
	public StatueBargainContainer(@Nullable MenuType<?> type, int id, Inventory playerInventory, @Nullable StatueDialog dialog, @Nullable ResourceLocation advancement){
		super(type, id);
		this.playerInventory = playerInventory;
		this.dialog = dialog;
		this.advancement = advancement;
		this.bargains = playerInventory.player.level.getRecipeManager()
				.getAllRecipesFor(Contents.STATUE_BARGAIN_RECIPE_TYPE)
				.stream()
				.filter(b -> type==null||b.getBargainOwner().equals(type.getRegistryName()))
				.sorted(Comparator.comparing(Recipe::getId))
				.collect(Collectors.toList());

		this.previousBargainTest = new Preview[bargains.size()];
		for(int i = 0; i<previousBargainTest.length; i++)
			this.previousBargainTest[i] = new Preview();
		this.inventoryCache = NonNullList.withSize(playerInventory.getContainerSize(), ItemStack.EMPTY);
		for(int i = 0; i<playerInventory.getContainerSize(); i++){
			ItemStack stack = playerInventory.getItem(i);
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

	@Nullable public Vec3 getLookAt(){
		return lookAt;
	}

	public void setLookAt(@Nullable Vec3 lookAt){
		this.lookAt = lookAt;
	}

	@Nullable public ResourceLocation getAdvancement(){
		return advancement;
	}
	public void setAdvancement(@Nullable ResourceLocation advancement){
		this.advancement = advancement;
	}

	@Override public boolean stillValid(Player playerIn){
		return true;
	}

	@Override public void broadcastChanges(){
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
		super.broadcastChanges();
	}

	private boolean inventoryChanged(){
		boolean changed = false;
		for(int i = 0; i<playerInventory.getContainerSize(); ++i){
			ItemStack s1 = this.playerInventory.getItem(i);
			ItemStack s2 = this.inventoryCache.get(i);
			if(!ItemStack.matches(s2, s1)){
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
		Component dialog = this.dialog.getDialog(playerInventory.player.getRandom(), dialogCase, bargain, result);
		if(dialog!=null) sendToPlayer(new StatueDialogMsg(dialog));
	}

	private void sendToPlayer(Object message){
		Player player = playerInventory.player;
		if(player instanceof ServerPlayer)
			ModNet.NET.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), message);
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

		public boolean updateCanBargain(StatueBargain bargain, Player player){
			boolean canBargain = bargain.bargain(player, true).isSuccess();
			if(this.canBargain==canBargain) return false;
			this.canBargain = canBargain;
			return true;
		}

		public boolean updateDemandPreview(StatueBargain bargain, Player player){
			ItemDemand[] demands = bargain
					.getPreview().demands()
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
