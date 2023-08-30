package datagen.builder;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.contents.Contents;

import java.util.Objects;
import java.util.function.Consumer;

public class CosmeticRecipeBuilder{
	private final Item result;
	private final Ingredient input;
	private final Ingredient reagent;
	private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
	private String group;

	private RecipeCategory recipeCategory = RecipeCategory.MISC;

	public CosmeticRecipeBuilder(Item result, Ingredient input, Ingredient reagent){
		this.result = result;
		this.input = input;
		this.reagent = reagent;
	}

	public CosmeticRecipeBuilder addCriterion(String name, CriterionTriggerInstance criterionIn){
		this.advancementBuilder.addCriterion(name, criterionIn);
		return this;
	}

	public CosmeticRecipeBuilder setGroup(String group){
		this.group = group;
		return this;
	}

	public CosmeticRecipeBuilder recipeCategory(RecipeCategory category){
		this.recipeCategory = Objects.requireNonNull(category);
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumer){
		this.build(consumer, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.result)));
	}

	public void build(Consumer<FinishedRecipe> consumer, String save){
		ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(this.result);
		if((new ResourceLocation(save)).equals(resourcelocation))
			throw new IllegalStateException("Paraglider Cosmetic Recipe "+save+" should remove its 'save' argument");
		this.build(consumer, new ResourceLocation(save));
	}

	public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id){
		this.validate(id);
		this.advancementBuilder.parent(new ResourceLocation("recipes/root"))
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
				.rewards(AdvancementRewards.Builder.recipe(id))
				.requirements(RequirementsStrategy.OR);
		consumer.accept(new Result(id,
				this.result,
				this.group==null ? "" : this.group,
				this.input,
				this.reagent,
				this.advancementBuilder,
				id.withPrefix("recipes/"+recipeCategory.getFolderName()+"/")));
	}

	private void validate(ResourceLocation id){
		if(this.advancementBuilder.getCriteria().isEmpty())
			throw new IllegalStateException("No way of obtaining recipe "+id);
	}

	public static class Result implements FinishedRecipe{
		private final ResourceLocation id;
		private final Item result;
		private final String group;
		private final Ingredient input;
		private final Ingredient reagent;
		private final Advancement.Builder advancementBuilder;
		private final ResourceLocation advancementId;

		public Result(ResourceLocation id, Item result, String group, Ingredient input, Ingredient reagent,
		              Advancement.Builder advancementBuilder, ResourceLocation advancementId){
			this.id = id;
			this.result = result;
			this.group = group;
			this.input = input;
			this.reagent = reagent;
			this.advancementBuilder = advancementBuilder;
			this.advancementId = advancementId;
		}

		@Override public void serializeRecipeData(@NotNull JsonObject json){
			if(!this.group.isEmpty()) json.addProperty("group", this.group);
			json.add("input", this.input.toJson());
			json.add("reagent", this.reagent.toJson());
			json.addProperty("result", Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.result)).toString());
		}

		@Override @NotNull public RecipeSerializer<?> getType(){
			return Contents.get().cosmeticRecipeSerializer();
		}
		@Override @NotNull public ResourceLocation getId(){
			return this.id;
		}
		@Override @Nullable public JsonObject serializeAdvancement(){
			return this.advancementBuilder.serializeToJson();
		}
		@Override @Nullable public ResourceLocation getAdvancementId(){
			return this.advancementId;
		}
	}
}
