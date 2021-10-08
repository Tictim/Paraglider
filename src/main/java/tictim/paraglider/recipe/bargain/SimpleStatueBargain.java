package tictim.paraglider.recipe.bargain;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.recipe.bargain.BargainResult.FailedReason;
import tictim.paraglider.utils.ParagliderUtils;
import tictim.paraglider.utils.QuantifiedIngredient;
import tictim.paraglider.utils.QuantifiedItem;
import tictim.paraglider.utils.TooltipFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleStatueBargain implements StatueBargain{
	private final ResourceLocation id;
	private final ResourceLocation bargainOwner;

	private final List<QuantifiedIngredient> itemDemands;
	private final int heartContainerDemands;
	private final int staminaVesselDemands;
	private final int essenceDemands;

	private final List<QuantifiedItem> itemOffers;
	private final int heartContainerOffers;
	private final int staminaVesselOffers;
	private final int essenceOffers;

	@Nullable private BargainPreview preview;

	public SimpleStatueBargain(ResourceLocation id,
	                           ResourceLocation bargainOwner,
	                           List<QuantifiedIngredient> itemDemands,
	                           int heartContainerDemands,
	                           int staminaVesselDemands,
	                           int essenceDemands,
	                           List<QuantifiedItem> itemOffers,
	                           int heartContainerOffers,
	                           int staminaVesselOffers,
	                           int essenceOffers){
		this.id = Objects.requireNonNull(id);
		this.bargainOwner = Objects.requireNonNull(bargainOwner);
		this.itemDemands = ImmutableList.copyOf(itemDemands);
		this.heartContainerDemands = heartContainerDemands;
		this.staminaVesselDemands = staminaVesselDemands;
		this.essenceDemands = essenceDemands;
		this.itemOffers = ImmutableList.copyOf(itemOffers);
		this.heartContainerOffers = heartContainerOffers;
		this.staminaVesselOffers = staminaVesselOffers;
		this.essenceOffers = essenceOffers;
	}

	@Override public ResourceLocation getBargainOwner(){
		return bargainOwner;
	}

	public List<QuantifiedIngredient> getItemDemands(){
		return itemDemands;
	}
	public int getHeartContainerDemands(){
		return heartContainerDemands;
	}
	public int getStaminaVesselDemands(){
		return staminaVesselDemands;
	}
	public int getEssenceDemands(){
		return essenceDemands;
	}

	public List<QuantifiedItem> getItemOffers(){
		return itemOffers;
	}
	public int getHeartContainerOffers(){
		return heartContainerOffers;
	}
	public int getStaminaVesselOffers(){
		return staminaVesselOffers;
	}
	public int getEssenceOffers(){
		return essenceOffers;
	}

	@Override public BargainPreview getPreview(){
		if(preview==null){
			List<BargainPreview.Demand> demands = new ArrayList<>();
			List<BargainPreview.Offer> offers = new ArrayList<>();

			for(QuantifiedIngredient itemDemand : itemDemands)
				demands.add(new BargainPreview.Demand(
						itemDemand.ingredient().getItems(),
						itemDemand.quantity(),
						p -> {
							int count = 0;
							for(int i = 0; i<p.getInventory().getContainerSize(); i++){
								ItemStack stack = p.getInventory().getItem(i);
								if(stack.isEmpty()||!itemDemand.test(stack)) continue;
								count += stack.getCount();
							}
							return count;
						}));
			if(heartContainerDemands>0)
				demands.add(new BargainPreview.Demand(
						new ItemStack(Contents.HEART_CONTAINER.get()),
						heartContainerDemands,
						p -> {
							ServerPlayerMovement m = ServerPlayerMovement.of(p);
							return m!=null ? m.getHeartContainers() : 0;
						},
						TooltipFactory.heartContainer(heartContainerDemands)));
			if(staminaVesselDemands>0)
				demands.add(new BargainPreview.Demand(
						new ItemStack(Contents.STAMINA_VESSEL.get()),
						staminaVesselDemands,
						p -> {
							ServerPlayerMovement m = ServerPlayerMovement.of(p);
							return m!=null ? m.getStaminaVessels() : 0;
						},
						TooltipFactory.staminaVessel(staminaVesselDemands)));
			if(essenceDemands>0)
				demands.add(new BargainPreview.Demand(
						new ItemStack(Contents.ESSENCE.get()),
						essenceDemands,
						p -> {
							ServerPlayerMovement m = ServerPlayerMovement.of(p);
							return m!=null ? m.getEssence() : 0;
						},
						TooltipFactory.essence(essenceDemands)));

			for(QuantifiedItem itemOffer : itemOffers)
				offers.add(new BargainPreview.Offer(new ItemStack(itemOffer.item()), itemOffer.quantity()));
			if(heartContainerOffers>0)
				offers.add(new BargainPreview.Offer(
						new ItemStack(Contents.HEART_CONTAINER.get()),
						heartContainerOffers,
						TooltipFactory.heartContainer(heartContainerOffers)));
			if(staminaVesselOffers>0)
				offers.add(new BargainPreview.Offer(
						new ItemStack(Contents.STAMINA_VESSEL.get()),
						staminaVesselOffers,
						TooltipFactory.staminaVessel(staminaVesselOffers)));
			if(essenceOffers>0)
				offers.add(new BargainPreview.Offer(
						new ItemStack(Contents.ESSENCE.get()),
						essenceOffers,
						TooltipFactory.essence(essenceOffers)));

			preview = new BargainPreview(demands, offers);
		}
		return preview;
	}

	public void invalidatePreview(){
		this.preview = null;
	}

	@Override public BargainResult bargain(Player player, boolean simulate){
		if(ServerPlayerMovement.of(player)==null)
			return BargainResult.failure(FailedReason.OTHER);

		Set<FailedReason> reasons = new HashSet<>();

		Inventory inventory = player.getInventory();
		int[] consumptions = new int[inventory.getContainerSize()];
		for(QuantifiedIngredient i : itemDemands)
			if(!test(i, inventory, consumptions))
				reasons.add(FailedReason.NOT_ENOUGH_ITEMS);

		if(!ParagliderUtils.takeHeartContainers(player, heartContainerDemands, true, false)) reasons.add(FailedReason.NOT_ENOUGH_HEART);
		if(!ParagliderUtils.takeStaminaVessels(player, staminaVesselDemands, true, false)) reasons.add(FailedReason.NOT_ENOUGH_STAMINA);
		if(!ParagliderUtils.takeEssences(player, essenceDemands, true, false)) reasons.add(FailedReason.NOT_ENOUGH_ESSENCE);

		if(!reasons.isEmpty()) return BargainResult.result(reasons);

		if(!ParagliderUtils.giveHeartContainers(player, heartContainerOffers-heartContainerDemands, true, false)) reasons.add(FailedReason.HEART_FULL);
		if(!ParagliderUtils.giveStaminaVessels(player, staminaVesselOffers-staminaVesselDemands, true, false)) reasons.add(FailedReason.STAMINA_FULL);
		if(!ParagliderUtils.giveEssences(player, essenceOffers-essenceDemands, true, false)) reasons.add(FailedReason.ESSENCE_FULL);

		if(!reasons.isEmpty()) return BargainResult.result(reasons);

		if(!simulate){
			for(int i = 0; i<consumptions.length; i++){
				int c = consumptions[i];
				if(c==0) continue;

				ItemStack stack = inventory.getItem(i);
				if(stack.getCount()>c) stack.shrink(c);
				else{
					if(stack.getCount()!=c)
						ParagliderMod.LOGGER.error("Quantity of item {} (slot number {}) differs from simulation.", stack, i);
					inventory.setItem(i, ItemStack.EMPTY);
				}
			}

			for(QuantifiedItem item : itemOffers)
				ParagliderUtils.giveItem(player, new ItemStack(item.item(), item.quantity()));
			if(heartContainerDemands!=heartContainerOffers&&!(heartContainerDemands<heartContainerOffers ?
					ParagliderUtils.giveHeartContainers(player, heartContainerOffers-heartContainerDemands, false, true) :
					ParagliderUtils.takeHeartContainers(player, heartContainerDemands-heartContainerOffers, false, true)))
				ParagliderMod.LOGGER.error("Heart Container demand of bargain {} failed to resolve after successful simulation.", id);
			if(staminaVesselDemands!=staminaVesselOffers&&!(staminaVesselDemands<staminaVesselOffers ?
					ParagliderUtils.giveStaminaVessels(player, staminaVesselOffers-staminaVesselDemands, false, true) :
					ParagliderUtils.takeStaminaVessels(player, staminaVesselDemands-staminaVesselOffers, false, true)))
				ParagliderMod.LOGGER.error("Stamina Vessel demand of bargain {} failed to resolve after successful simulation.", id);
			if(essenceDemands!=essenceOffers&&!(essenceDemands<essenceOffers ?
					ParagliderUtils.giveEssences(player, essenceOffers-essenceDemands, false, true) :
					ParagliderUtils.takeEssences(player, essenceDemands-essenceOffers, false, true)))
				ParagliderMod.LOGGER.error("Essence demand of bargain {} failed to resolve after successful simulation.", id);
		}
		return BargainResult.success();
	}

	private static boolean test(QuantifiedIngredient quantifiedIngredient, Container inventory, int[] consumptions){
		int amountLeft = quantifiedIngredient.quantity();
		for(int i = 0; amountLeft>0&&i<inventory.getContainerSize(); i++){
			ItemStack stack = inventory.getItem(i);
			if(stack.getCount()<=consumptions[i]||!quantifiedIngredient.test(stack)) continue;
			int amountToConsume = Math.min(amountLeft, stack.getCount()-consumptions[i]);
			amountLeft -= amountToConsume;
			consumptions[i] += amountToConsume;
		}
		return amountLeft<=0;
	}

	@Override public boolean consumesItem(){
		return !itemDemands.isEmpty();
	}
	@Override public boolean consumesHeartContainer(){
		return heartContainerDemands>0;
	}
	@Override public boolean consumesStaminaVessel(){
		return staminaVesselDemands>0;
	}
	@Override public boolean consumesEssence(){
		return essenceDemands>0;
	}
	@Override public boolean givesItem(){
		return !itemOffers.isEmpty();
	}
	@Override public boolean givesHeartContainer(){
		return heartContainerOffers>0;
	}
	@Override public boolean givesStaminaVessel(){
		return staminaVesselOffers>0;
	}
	@Override public boolean givesEssence(){
		return essenceOffers>0;
	}

	@Override public ResourceLocation getId(){
		return id;
	}
	@Override public RecipeSerializer<?> getSerializer(){
		return Contents.STATUE_BARGAIN_RECIPE.get();
	}
	@Override public RecipeType<?> getType(){
		return Contents.STATUE_BARGAIN_RECIPE_TYPE;
	}

	@Override public String toString(){
		return "SimpleStatueBargain{"+
				"id="+id+
				", bargainOwner="+bargainOwner+
				", itemDemands="+itemDemands.stream().map(it -> it.toString()).collect(Collectors.joining(", "))+
				", heartContainerDemands="+heartContainerDemands+
				", staminaVesselDemands="+staminaVesselDemands+
				", essenceDemands="+essenceDemands+
				", itemOffers="+itemOffers.stream().map(it -> it.toString()).collect(Collectors.joining(", "))+
				", heartContainerOffers="+heartContainerOffers+
				", staminaVesselOffers="+staminaVesselOffers+
				", essenceOffers="+essenceOffers+
				'}';
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<SimpleStatueBargain>{
		@Override public SimpleStatueBargain fromJson(ResourceLocation recipeId, JsonObject json){
			final ResourceLocation bargainOwner = new ResourceLocation(GsonHelper.getAsString(json, "owner"));
			final List<QuantifiedIngredient> itemDemands;
			final int heartContainerDemands;
			final int staminaVesselDemands;
			final int essenceDemands;
			final List<QuantifiedItem> itemOffers;
			final int heartContainerOffers;
			final int staminaVesselOffers;
			final int essenceOffers;

			@SuppressWarnings("ConstantConditions")
			JsonObject demands = GsonHelper.getAsJsonObject(json, "demands", null);
			//noinspection ConstantConditions
			if(demands!=null){
				JsonArray items = GsonHelper.getAsJsonArray(demands, "items", null);
				if(items==null||items.size()==0) itemDemands = Collections.emptyList();
				else{
					itemDemands = new ArrayList<>();
					for(JsonElement i : items)
						itemDemands.add(new QuantifiedIngredient(GsonHelper.convertToJsonObject(i, "item")));
				}
				heartContainerDemands = Math.max(0, GsonHelper.getAsInt(demands, "heartContainers", 0));
				staminaVesselDemands = Math.max(0, GsonHelper.getAsInt(demands, "staminaVessels", 0));
				essenceDemands = Math.max(0, GsonHelper.getAsInt(demands, "essences", 0));
			}else{
				itemDemands = Collections.emptyList();
				heartContainerDemands = 0;
				staminaVesselDemands = 0;
				essenceDemands = 0;
			}

			@SuppressWarnings("ConstantConditions")
			JsonObject offers = GsonHelper.getAsJsonObject(json, "offers", null);
			//noinspection ConstantConditions
			if(offers!=null){
				JsonArray items = GsonHelper.getAsJsonArray(offers, "items", null);
				if(items==null||items.size()==0) itemOffers = Collections.emptyList();
				else{
					itemOffers = new ArrayList<>();
					for(JsonElement i : items){
						ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(i, "item"));
						itemOffers.add(new QuantifiedItem(stack.getItem(), stack.getCount()));
					}
				}
				heartContainerOffers = Math.max(0, GsonHelper.getAsInt(offers, "heartContainers", 0));
				staminaVesselOffers = Math.max(0, GsonHelper.getAsInt(offers, "staminaVessels", 0));
				essenceOffers = Math.max(0, GsonHelper.getAsInt(offers, "essences", 0));
			}else{
				itemOffers = Collections.emptyList();
				heartContainerOffers = 0;
				staminaVesselOffers = 0;
				essenceOffers = 0;
			}

			return new SimpleStatueBargain(recipeId,
					bargainOwner,
					itemDemands,
					heartContainerDemands,
					staminaVesselDemands,
					essenceDemands,
					itemOffers,
					heartContainerOffers,
					staminaVesselOffers,
					essenceOffers);
		}

		@Nullable @Override public SimpleStatueBargain fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer){
			ResourceLocation owner = buffer.readResourceLocation();

			List<QuantifiedIngredient> itemDemands = new ArrayList<>();
			for(int i = 0, size = buffer.readVarInt(); i<size; i++)
				itemDemands.add(QuantifiedIngredient.read(buffer));
			int heartContainerDemands = buffer.readVarInt();
			int staminaVesselDemands = buffer.readVarInt();
			int essenceDemands = buffer.readVarInt();

			List<QuantifiedItem> itemOffers = new ArrayList<>();
			for(int i = 0, size = buffer.readVarInt(); i<size; i++)
				itemOffers.add(QuantifiedItem.read(buffer));

			int heartContainerOffers = buffer.readVarInt();
			int staminaVesselOffers = buffer.readVarInt();
			int essenceOffers = buffer.readVarInt();

			return new SimpleStatueBargain(
					recipeId,
					owner,
					itemDemands,
					heartContainerDemands,
					staminaVesselDemands,
					essenceDemands,
					itemOffers,
					heartContainerOffers,
					staminaVesselOffers,
					essenceOffers);
		}

		@Override public void toNetwork(FriendlyByteBuf buffer, SimpleStatueBargain recipe){
			buffer.writeResourceLocation(recipe.getBargainOwner());

			List<QuantifiedIngredient> itemDemands = recipe.getItemDemands();
			buffer.writeVarInt(itemDemands.size());
			for(QuantifiedIngredient demand : itemDemands)
				demand.write(buffer);
			buffer.writeVarInt(recipe.getHeartContainerDemands());
			buffer.writeVarInt(recipe.getStaminaVesselDemands());
			buffer.writeVarInt(recipe.getEssenceDemands());

			List<QuantifiedItem> itemOffers = recipe.getItemOffers();
			buffer.writeVarInt(itemOffers.size());
			for(QuantifiedItem offer : itemOffers)
				offer.write(buffer);
			buffer.writeVarInt(recipe.getHeartContainerOffers());
			buffer.writeVarInt(recipe.getStaminaVesselOffers());
			buffer.writeVarInt(recipe.getEssenceOffers());
		}
	}
}
