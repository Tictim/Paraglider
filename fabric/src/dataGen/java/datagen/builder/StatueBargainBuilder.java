package datagen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;
import tictim.paraglider.contents.Contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StatueBargainBuilder{
	protected final ResourceLocation bargainType;

	protected final List<QuantifiedIngredient> itemDemands = new ArrayList<>();
	protected int heartContainerDemands;
	protected int staminaVesselDemands;
	protected int essenceDemands;

	protected final List<QuantifiedItem> itemOffers = new ArrayList<>();
	protected int heartContainerOffers;
	protected int staminaVesselOffers;
	protected int essenceOffers;

	protected boolean featureFlags;

	public StatueBargainBuilder(ResourceLocation bargainType){
		this.bargainType = Objects.requireNonNull(bargainType);
	}

	public StatueBargainBuilder demand(ItemLike item, int quantity){
		return demand(Ingredient.of(item), quantity);
	}
	public StatueBargainBuilder demand(TagKey<Item> tag, int quantity){
		return demand(Ingredient.of(tag), quantity);
	}
	public StatueBargainBuilder demand(Ingredient ingredient, int quantity){
		itemDemands.add(new QuantifiedIngredient(ingredient, quantity));
		return this;
	}
	public StatueBargainBuilder demandHeartContainer(int quantity){
		heartContainerDemands = quantity;
		return this;
	}
	public StatueBargainBuilder demandStaminaVessel(int quantity){
		staminaVesselDemands = quantity;
		return this;
	}
	public StatueBargainBuilder demandEssence(int quantity){
		essenceDemands = quantity;
		return this;
	}

	public StatueBargainBuilder offer(Item item, int count){
		itemOffers.add(new QuantifiedItem(item, count));
		return this;
	}
	public StatueBargainBuilder offerHeartContainer(int quantity){
		heartContainerOffers = quantity;
		return this;
	}
	public StatueBargainBuilder offerStaminaVessel(int quantity){
		staminaVesselOffers = quantity;
		return this;
	}
	public StatueBargainBuilder offerEssence(int quantity){
		essenceOffers = quantity;
		return this;
	}

	public StatueBargainBuilder disableFeatureFlags(){
		featureFlags = false;
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id){
		consumerIn.accept(new Result(id,
				bargainType,
				itemDemands,
				heartContainerDemands,
				staminaVesselDemands,
				essenceDemands,
				itemOffers,
				heartContainerOffers,
				staminaVesselOffers,
				essenceOffers,
				featureFlags));
	}

	public static class Result implements FinishedRecipe{
		protected final ResourceLocation id;
		protected final ResourceLocation bargainType;

		protected final List<QuantifiedIngredient> itemDemands;
		protected final int heartContainerDemands;
		protected final int staminaVesselDemands;
		protected final int essenceDemands;

		protected final List<QuantifiedItem> itemOffers;
		protected final int heartContainerOffers;
		protected final int staminaVesselOffers;
		protected final int essenceOffers;

		protected final boolean disableFlags;

		public Result(@NotNull ResourceLocation id,
		              @NotNull ResourceLocation bargainType,
		              @NotNull List<QuantifiedIngredient> itemDemands,
		              int heartContainerDemands,
		              int staminaVesselDemands,
		              int essenceDemands,
		              @NotNull List<QuantifiedItem> itemOffers,
		              int heartContainerOffers,
		              int staminaVesselOffers,
		              int essenceOffers,
		              boolean disableFlags){
			this.id = id;
			this.bargainType = bargainType;
			this.itemDemands = itemDemands;
			this.heartContainerDemands = heartContainerDemands;
			this.staminaVesselDemands = staminaVesselDemands;
			this.essenceDemands = essenceDemands;
			this.itemOffers = itemOffers;
			this.heartContainerOffers = heartContainerOffers;
			this.staminaVesselOffers = staminaVesselOffers;
			this.essenceOffers = essenceOffers;
			this.disableFlags = disableFlags;
		}

		@Override public void serializeRecipeData(@NotNull JsonObject json){
			json.addProperty("bargainType", bargainType.toString());
			if(!itemDemands.isEmpty()||heartContainerDemands>0||staminaVesselDemands>0||essenceDemands>0){
				JsonObject demands = new JsonObject();
				if(!itemDemands.isEmpty())
					demands.add("items", itemDemands.stream().collect(JsonArray::new, (e, i) -> e.add(i.serialize()), (e1, e2) -> {}));
				if(heartContainerDemands>0) demands.addProperty("heartContainers", heartContainerDemands);
				if(staminaVesselDemands>0) demands.addProperty("staminaVessels", staminaVesselDemands);
				if(essenceDemands>0) demands.addProperty("essences", essenceDemands);
				json.add("demands", demands);
			}
			if(!itemOffers.isEmpty()||heartContainerOffers>0||staminaVesselOffers>0||essenceOffers>0){
				JsonObject offers = new JsonObject();
				if(!itemOffers.isEmpty())
					offers.add("items", itemOffers.stream().collect(JsonArray::new, (e, i) -> e.add(i.serialize()), (e1, e2) -> {}));
				if(heartContainerOffers>0) offers.addProperty("heartContainers", heartContainerOffers);
				if(staminaVesselOffers>0) offers.addProperty("staminaVessels", staminaVesselOffers);
				if(essenceOffers>0) offers.addProperty("essences", essenceOffers);
				json.add("offers", offers);
			}
			if(!this.disableFlags){
				if(heartContainerDemands>0||heartContainerOffers>0) json.addProperty("usesHeartContainerFeature", true);
				if(staminaVesselDemands>0||staminaVesselOffers>0) json.addProperty("usesStaminaVesselFeature", true);
			}
		}
		@Override @NotNull public ResourceLocation getId(){
			return id;
		}
		@Override @NotNull public RecipeSerializer<?> getType(){
			return Contents.get().bargainRecipeSerializer();
		}
		@Override @Nullable public JsonObject serializeAdvancement(){
			return null;
		}
		@Override @Nullable public ResourceLocation getAdvancementId(){
			return null;
		}
	}
}
