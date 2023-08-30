package tictim.paraglider.contents.recipe;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainResult;
import tictim.paraglider.api.bargain.DemandPreview;
import tictim.paraglider.api.bargain.OfferPreview;
import tictim.paraglider.api.bargain.ParagliderBargainTags;
import tictim.paraglider.api.bargain.ParagliderFailReasons;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.bargain.preview.EssenceDemandPreview;
import tictim.paraglider.bargain.preview.EssenceOfferPreview;
import tictim.paraglider.bargain.preview.HeartContainerDemandPreview;
import tictim.paraglider.bargain.preview.HeartContainerOfferPreview;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;
import tictim.paraglider.bargain.preview.StaminaVesselDemandPreview;
import tictim.paraglider.bargain.preview.StaminaVesselOfferPreview;
import tictim.paraglider.contents.Contents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SimpleBargain implements Bargain{
	private final ResourceLocation id;
	private final ResourceLocation bargainType;

	private final List<QuantifiedIngredient> itemDemands;
	private final int heartContainerDemands;
	private final int staminaVesselDemands;
	private final int essenceDemands;

	private final List<QuantifiedItem> itemOffers;
	private final int heartContainerOffers;
	private final int staminaVesselOffers;
	private final int essenceOffers;

	private final Set<String> userTags;
	private final Set<String> tags;

	@Nullable private List<DemandPreview> demandPreviews;
	@Nullable private List<OfferPreview> offerPreviews;

	public SimpleBargain(@NotNull ResourceLocation id,
	                     @NotNull ResourceLocation bargainType,
	                     @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
	                     int heartContainerDemands,
	                     int staminaVesselDemands,
	                     int essenceDemands,
	                     @NotNull List<@NotNull QuantifiedItem> itemOffers,
	                     int heartContainerOffers,
	                     int staminaVesselOffers,
	                     int essenceOffers,
	                     @NotNull Set<@NotNull String> userTags){
		this.id = Objects.requireNonNull(id);
		this.bargainType = bargainType;
		this.itemDemands = ImmutableList.copyOf(itemDemands);
		for(var itemDemand : this.itemDemands) Objects.requireNonNull(itemDemand);
		this.heartContainerDemands = heartContainerDemands;
		this.staminaVesselDemands = staminaVesselDemands;
		this.essenceDemands = essenceDemands;
		this.itemOffers = ImmutableList.copyOf(itemOffers);
		this.heartContainerOffers = heartContainerOffers;
		this.staminaVesselOffers = staminaVesselOffers;
		this.essenceOffers = essenceOffers;

		this.userTags = Set.copyOf(userTags);
		this.tags = new ObjectOpenHashSet<>(userTags);

		if(!this.itemDemands.isEmpty()) this.tags.add(ParagliderBargainTags.CONSUMES_ITEM);
		if(this.heartContainerDemands>0) this.tags.add(ParagliderBargainTags.CONSUMES_HEART_CONTAINER);
		if(this.staminaVesselDemands>0) this.tags.add(ParagliderBargainTags.CONSUMES_STAMINA_VESSEL);
		if(this.essenceDemands>0) this.tags.add(ParagliderBargainTags.CONSUMES_ESSENCE);
		if(!this.itemOffers.isEmpty()) this.tags.add(ParagliderBargainTags.GIVES_ITEM);
		if(this.heartContainerOffers>0) this.tags.add(ParagliderBargainTags.GIVES_HEART_CONTAINER);
		if(this.staminaVesselOffers>0) this.tags.add(ParagliderBargainTags.GIVES_STAMINA_VESSEL);
		if(this.essenceOffers>0) this.tags.add(ParagliderBargainTags.GIVES_ESSENCE);
	}

	@Override @NotNull public ResourceLocation getBargainType(){
		return bargainType;
	}

	@NotNull public List<QuantifiedIngredient> getItemDemands(){
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

	@NotNull public List<QuantifiedItem> getItemOffers(){
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

	@NotNull public Set<String> getUserTags(){
		return userTags;
	}

	@Override public boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos){
		return true;
	}

	@Override @NotNull @Unmodifiable public List<@NotNull DemandPreview> previewDemands(){
		if(this.demandPreviews!=null) return this.demandPreviews;
		this.demandPreviews = new ArrayList<>();

		this.demandPreviews.addAll(this.itemDemands);
		if(heartContainerDemands>0){
			this.demandPreviews.add(new HeartContainerDemandPreview(heartContainerDemands));
		}
		if(staminaVesselDemands>0){
			this.demandPreviews.add(new StaminaVesselDemandPreview(staminaVesselDemands));
		}
		if(essenceDemands>0){
			this.demandPreviews.add(new EssenceDemandPreview(essenceDemands));
		}
		return this.demandPreviews;
	}
	@Override @NotNull @Unmodifiable public List<@NotNull OfferPreview> previewOffers(){
		if(this.offerPreviews!=null) return this.offerPreviews;
		this.offerPreviews = new ArrayList<>();

		this.offerPreviews.addAll(itemOffers);
		if(heartContainerOffers>0){
			this.offerPreviews.add(new HeartContainerOfferPreview(heartContainerOffers));
		}
		if(staminaVesselOffers>0){
			this.offerPreviews.add(new StaminaVesselOfferPreview(staminaVesselOffers));
		}
		if(essenceOffers>0){
			this.offerPreviews.add(new EssenceOfferPreview(essenceOffers));
		}
		return this.offerPreviews;
	}

	@Override @NotNull public BargainResult bargain(@NotNull Player player, boolean simulate){
		VesselContainer container = VesselContainer.get(player);

		Set<String> reasons = new HashSet<>();

		Inventory inventory = player.getInventory();
		var consumptions = new Int2IntOpenHashMap();
		for(QuantifiedIngredient i : itemDemands){
			if(!ParagliderUtils.calculateConsumption(i, inventory, consumptions)){
				reasons.add(ParagliderFailReasons.NOT_ENOUGH_ITEMS);
				break;
			}
		}

		if(container.takeHeartContainers(heartContainerDemands, true, false)<heartContainerDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_HEARTS);
		if(container.takeStaminaVessels(staminaVesselDemands, true, false)<staminaVesselDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_STAMINA);
		if(container.takeEssences(essenceDemands, true, false)<essenceDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_ESSENCES);

		if(!reasons.isEmpty()) return BargainResult.fail(reasons);

		int heartDiff = heartContainerOffers-heartContainerDemands;
		int staminaDiff = staminaVesselOffers-staminaVesselDemands;
		int essenceDiff = essenceOffers-essenceDemands;

		if(heartDiff>0&&container.giveHeartContainers(heartDiff, true, false)<heartDiff)
			reasons.add(ParagliderFailReasons.HEART_FULL);
		if(staminaDiff>0&&container.giveStaminaVessels(staminaDiff, true, false)<staminaDiff)
			reasons.add(ParagliderFailReasons.STAMINA_FULL);
		if(essenceDiff>0&&container.giveEssences(essenceDiff, true, false)<essenceDiff)
			reasons.add(ParagliderFailReasons.ESSENCE_FULL);

		if(!reasons.isEmpty()) return BargainResult.fail(reasons);

		if(!simulate){
			for(var e : consumptions.int2IntEntrySet()){
				int i = e.getIntKey();
				int c = e.getIntValue();
				if(c<=0) continue;

				ItemStack stack = inventory.getItem(i);
				if(stack.getCount()>c) stack.shrink(c);
				else{
					if(stack.getCount()!=c)
						ParagliderMod.LOGGER.error("Quantity of item {} (slot number {}) differs from simulation.", stack, i);
					inventory.setItem(i, ItemStack.EMPTY);
				}
			}

			for(QuantifiedItem item : itemOffers){
				ParagliderUtils.giveItem(player, item.getItemWithQuantity());
			}

			if(heartDiff!=0){
				if(heartDiff>0 ?
						container.giveHeartContainers(heartDiff, false, true)!=heartDiff :
						container.takeHeartContainers(-heartDiff, false, true)!=-heartDiff)
					ParagliderMod.LOGGER.error("Heart Container transaction of bargain {} failed to resolve after successful simulation.", id);
			}
			if(staminaDiff!=0){
				if(staminaDiff>0 ?
						container.giveStaminaVessels(staminaDiff, false, true)!=staminaDiff :
						container.takeStaminaVessels(-staminaDiff, false, true)!=-staminaDiff)
					ParagliderMod.LOGGER.error("Stamina Vessel transaction of bargain {} failed to resolve after successful simulation.", id);
			}
			if(essenceDiff!=0){
				if(essenceDiff>0 ?
						container.giveEssences(essenceDiff, false, true)!=essenceDiff :
						container.takeEssences(-essenceDiff, false, true)!=-essenceDiff)
					ParagliderMod.LOGGER.error("Essence transaction of bargain {} failed to resolve after successful simulation.", id);
			}
		}
		return BargainResult.success();
	}

	@Override @NotNull @Unmodifiable public Set<@NotNull String> getBargainTags(){
		return Collections.unmodifiableSet(this.tags);
	}

	@Override @NotNull public ResourceLocation getId(){
		return id;
	}
	@Override @NotNull public RecipeSerializer<?> getSerializer(){
		return Contents.get().bargainRecipeSerializer();
	}
	@Override @NotNull public RecipeType<?> getType(){
		return Contents.get().bargainRecipeType();
	}
}
