package datagen.builder;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CosmeticRecipeBuilder{
	private final Item result;
	private final Ingredient input;
	private final Ingredient reagent;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
	private String group;

	private RecipeCategory recipeCategory = RecipeCategory.MISC;

	public CosmeticRecipeBuilder(Item result, Ingredient input, Ingredient reagent){
		this.result = result;
		this.input = input;
		this.reagent = reagent;
	}

	public CosmeticRecipeBuilder addCriterion(String name, Criterion<InventoryChangeTrigger.TriggerInstance> criterion){
		this.criteria.put(name, criterion);
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

	public void save(RecipeOutput recipeOutput){
		this.save(recipeOutput, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)));
	}

	public void save(RecipeOutput recipeOutput, String save){
		ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);
		if((new ResourceLocation(save)).equals(resourcelocation))
			throw new IllegalStateException("Paraglider Cosmetic Recipe "+save+" should remove its 'save' argument");
		this.save(recipeOutput, new ResourceLocation(save));
	}

	public void save(RecipeOutput recipeOutput, ResourceLocation id){
		this.validate(id);
		Advancement.Builder builder = recipeOutput.advancement()
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
				.rewards(AdvancementRewards.Builder.recipe(id))
				.requirements(AdvancementRequirements.Strategy.OR);
		recipeOutput.accept(new Result(id,
				this.result,
				this.group==null ? "" : this.group,
				this.input,
				this.reagent,
				builder.build(id.withPrefix("recipes/"+recipeCategory.getFolderName()+"/"))));
	}

	private void validate(ResourceLocation id){
		if(this.criteria.isEmpty())
			throw new IllegalStateException("No way of obtaining recipe "+id);
	}

	public record Result(@NotNull ResourceLocation id, Item result, String group, Ingredient input, Ingredient reagent, @Nullable AdvancementHolder advancement) implements FinishedRecipe{
		@Override public void serializeRecipeData(@NotNull JsonObject json){
			if(!this.group.isEmpty()) json.addProperty("group", this.group);
			json.add("input", this.input.toJson(false));
			json.add("reagent", this.reagent.toJson(false));
			json.addProperty("result", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)).toString());
		}

		@Override @NotNull public RecipeSerializer<?> type(){
			return Contents.get().cosmeticRecipeSerializer();
		}
	}
}
