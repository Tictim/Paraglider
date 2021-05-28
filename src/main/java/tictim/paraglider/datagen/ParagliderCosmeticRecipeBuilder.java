package tictim.paraglider.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class ParagliderCosmeticRecipeBuilder{
	private final Item result;
	private final Ingredient reagent;
	private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
	private String group;

	public ParagliderCosmeticRecipeBuilder(Item result, Ingredient reagent){
		this.result = result;
		this.reagent = reagent;
	}

	public ParagliderCosmeticRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn){
		this.advancementBuilder.withCriterion(name, criterionIn);
		return this;
	}

	public ParagliderCosmeticRecipeBuilder setGroup(String group){
		this.group = group;
		return this;
	}

	public void build(Consumer<IFinishedRecipe> consumerIn){
		this.build(consumerIn, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)));
	}

	public void build(Consumer<IFinishedRecipe> consumerIn, String save){
		ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);
		if((new ResourceLocation(save)).equals(resourcelocation))
			throw new IllegalStateException("Paraglider Cosmetic Recipe "+save+" should remove its 'save' argument");
		this.build(consumerIn, new ResourceLocation(save));
	}

	public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id){
		this.validate(id);
		this.advancementBuilder.withParentId(new ResourceLocation("recipes/root"))
				.withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
				.withRewards(AdvancementRewards.Builder.recipe(id))
				.withRequirementsStrategy(IRequirementsStrategy.OR);
		consumerIn.accept(new Result(id,
				this.result,
				this.group==null ? "" : this.group,
				this.reagent,
				this.advancementBuilder,
				new ResourceLocation(id.getNamespace(), "recipes/"+Objects.requireNonNull(this.result.getGroup()).getPath()+"/"+id.getPath())));
	}

	private void validate(ResourceLocation id){
		if(this.advancementBuilder.getCriteria().isEmpty())
			throw new IllegalStateException("No way of obtaining recipe "+id);
	}

	public static class Result implements IFinishedRecipe{
		private final ResourceLocation id;
		private final Item result;
		private final String group;
		private final Ingredient reagent;
		private final Advancement.Builder advancementBuilder;
		private final ResourceLocation advancementId;

		public Result(ResourceLocation idIn, Item resultIn, String groupIn, Ingredient reagent, Advancement.Builder advancementBuilderIn, ResourceLocation advancementIdIn){
			this.id = idIn;
			this.result = resultIn;
			this.group = groupIn;
			this.reagent = reagent;
			this.advancementBuilder = advancementBuilderIn;
			this.advancementId = advancementIdIn;
		}

		@Override public void serialize(JsonObject json){
			if(!this.group.isEmpty()) json.addProperty("group", this.group);
			json.add("reagent", this.reagent.serialize());
			json.addProperty("result", Objects.requireNonNull(this.result.getRegistryName()).toString());
		}

		@Override public IRecipeSerializer<?> getSerializer(){
			return Contents.PARAGLIDER_COSMETIC_RECIPE.get();
		}
		@Override public ResourceLocation getID(){
			return this.id;
		}
		@Override @Nullable public JsonObject getAdvancementJson(){
			return this.advancementBuilder.serialize();
		}
		@Override @Nullable public ResourceLocation getAdvancementID(){
			return this.advancementId;
		}
	}
}
