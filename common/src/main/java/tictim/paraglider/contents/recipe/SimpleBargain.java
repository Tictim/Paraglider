package tictim.paraglider.contents.recipe;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.experimental.Accessors;
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

@Getter
public class SimpleBargain implements Bargain{
	private final ResourceLocation bargainType;

	private final Demand demand;

	private final Offer offer;

	private final Set<String> userTags;
	private final Set<String> tags;

	@Nullable private List<DemandPreview> demandPreviews;
	@Nullable private List<OfferPreview> offerPreviews;

	public SimpleBargain(
	                     @NotNull ResourceLocation bargainType,
	                     @NotNull Demand demand,
	                     @NotNull Offer offer,
	                     @NotNull Set<@NotNull String> userTags){
		this.bargainType = bargainType;
		for(var itemDemand : demand.items) Objects.requireNonNull(itemDemand);
		this.demand = demand;
		this.offer = offer;

		this.userTags = Set.copyOf(userTags);
		this.tags = new ObjectOpenHashSet<>(userTags);

		if(!demand.items.isEmpty()) this.tags.add(ParagliderBargainTags.CONSUMES_ITEM);
		if(demand.heartContainers>0) this.tags.add(ParagliderBargainTags.CONSUMES_HEART_CONTAINER);
		if(demand.staminaVessels>0) this.tags.add(ParagliderBargainTags.CONSUMES_STAMINA_VESSEL);
		if(demand.essences>0) this.tags.add(ParagliderBargainTags.CONSUMES_ESSENCE);
		if(!offer.items.isEmpty()) this.tags.add(ParagliderBargainTags.GIVES_ITEM);
		if(offer.heartContainers>0) this.tags.add(ParagliderBargainTags.GIVES_HEART_CONTAINER);
		if(offer.staminaVessels>0) this.tags.add(ParagliderBargainTags.GIVES_STAMINA_VESSEL);
		if(offer.essences>0) this.tags.add(ParagliderBargainTags.GIVES_ESSENCE);
	}

	@Override public boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos){
		return true;
	}

	@Override @NotNull @Unmodifiable public List<@NotNull DemandPreview> previewDemands(){
		if(this.demandPreviews!=null) return this.demandPreviews;
		this.demandPreviews = new ArrayList<>();

		this.demandPreviews.addAll(demand.items);
		if(demand.heartContainers>0){
			this.demandPreviews.add(new HeartContainerDemandPreview(demand.heartContainers));
		}
		if(demand.staminaVessels>0){
			this.demandPreviews.add(new StaminaVesselDemandPreview(demand.staminaVessels));
		}
		if(demand.essences>0){
			this.demandPreviews.add(new EssenceDemandPreview(demand.essences));
		}
		return Collections.unmodifiableList(this.demandPreviews);
	}
	@Override @NotNull @Unmodifiable public List<@NotNull OfferPreview> previewOffers(){
		if(this.offerPreviews!=null) return this.offerPreviews;
		this.offerPreviews = new ArrayList<>();

		this.offerPreviews.addAll(offer.items);
		if(offer.heartContainers>0){
			this.offerPreviews.add(new HeartContainerOfferPreview(offer.heartContainers));
		}
		if(offer.staminaVessels>0){
			this.offerPreviews.add(new StaminaVesselOfferPreview(offer.staminaVessels));
		}
		if(offer.essences>0){
			this.offerPreviews.add(new EssenceOfferPreview(offer.essences));
		}
		return Collections.unmodifiableList(this.offerPreviews);
	}

	@Override @NotNull public BargainResult bargain(@NotNull Player player, boolean simulate){
		VesselContainer container = VesselContainer.get(player);

		Set<String> reasons = new HashSet<>();

		Inventory inventory = player.getInventory();
		var consumptions = new Int2IntOpenHashMap();
		for(QuantifiedIngredient i : demand.items){
			if(!ParagliderUtils.calculateConsumption(i, inventory, consumptions)){
				reasons.add(ParagliderFailReasons.NOT_ENOUGH_ITEMS);
				break;
			}
		}

		if(container.takeHeartContainers(demand.heartContainers, true, false)<demand.heartContainers)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_HEARTS);
		if(container.takeStaminaVessels(demand.staminaVessels, true, false)<demand.staminaVessels)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_STAMINA);
		if(container.takeEssences(demand.essences, true, false)<demand.essences)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_ESSENCES);

		if(!reasons.isEmpty()) return BargainResult.fail(reasons);

		int heartDiff = offer.heartContainers-demand.heartContainers;
		int staminaDiff = offer.staminaVessels-demand.staminaVessels;
		int essenceDiff = offer.essences-demand.essences;

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

			for(QuantifiedItem item : offer.items){
				ParagliderUtils.giveItem(player, item.getItemWithQuantity());
			}

			if(heartDiff!=0){
				if(heartDiff>0 ?
						container.giveHeartContainers(heartDiff, false, true)!=heartDiff :
						container.takeHeartContainers(-heartDiff, false, true)!=-heartDiff)
					ParagliderMod.LOGGER.error("Heart Container transaction of bargain failed to resolve after successful simulation.");
			}
			if(staminaDiff!=0){
				if(staminaDiff>0 ?
						container.giveStaminaVessels(staminaDiff, false, true)!=staminaDiff :
						container.takeStaminaVessels(-staminaDiff, false, true)!=-staminaDiff)
					ParagliderMod.LOGGER.error("Stamina Vessel transaction of bargain failed to resolve after successful simulation.");
			}
			if(essenceDiff!=0){
				if(essenceDiff>0 ?
						container.giveEssences(essenceDiff, false, true)!=essenceDiff :
						container.takeEssences(-essenceDiff, false, true)!=-essenceDiff)
					ParagliderMod.LOGGER.error("Essence transaction of bargain failed to resolve after successful simulation.");
			}
		}
		return BargainResult.success();
	}

	@Override @NotNull @Unmodifiable public Set<@NotNull String> getBargainTags(){
		return Collections.unmodifiableSet(this.tags);
	}

	@Override @NotNull public RecipeSerializer<?> getSerializer(){
		return Contents.get().bargainRecipeSerializer();
	}
	@Override @NotNull public RecipeType<?> getType(){
		return Contents.get().bargainRecipeType();
	}

	public static final class Demand extends Bargained<QuantifiedIngredient> {
		public Demand(List<QuantifiedIngredient> items, int heartContainers, int staminaVessels, int essences) {
			super(items, heartContainers, staminaVessels, essences);
		}

		public static final Codec<Demand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				QuantifiedIngredient.CODEC.listOf().fieldOf("items").forGetter(Bargained::items)
			).and(commonFields(instance)
			).apply(instance, Demand::new)
		);
	}

	public static final class Offer extends Bargained<QuantifiedItem> {
		public Offer(List<QuantifiedItem> items, int heartContainers, int staminaVessels, int essences) {
			super(items, heartContainers, staminaVessels, essences);
		}

		public static final Codec<Offer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				QuantifiedItem.CODEC.listOf().fieldOf("items").forGetter(Bargained::items)
			).and(commonFields(instance)
			).apply(instance, Offer::new)
		);
	}

	@Getter
	@Accessors(fluent = true)
	private static abstract class Bargained<Q> {
		protected final List<Q> items;
		protected final int heartContainers;
		protected final int staminaVessels;
		protected final int essences;

		private Bargained(@NotNull List<@NotNull Q> items, int heartContainers, int staminaVessels, int essences) {
			this.items = ImmutableList.copyOf(items);
			this.heartContainers = heartContainers;
			this.staminaVessels = staminaVessels;
			this.essences = essences;
		}

		@NotNull protected static <QQ, B extends Bargained<QQ>> Products.P3<RecordCodecBuilder.Mu<B>, Integer, Integer, Integer> commonFields(RecordCodecBuilder.Instance<B> instance) {
			return instance.group(
				Codec.INT.fieldOf("heartContainers").forGetter(Bargained::heartContainers),
				Codec.INT.fieldOf("staminaVessels").forGetter(Bargained::staminaVessels),
				Codec.INT.fieldOf("essences").forGetter(Bargained::essences));
		}
	}
}
