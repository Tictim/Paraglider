package datagen.builder;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tictim.paraglider.contents.recipe.CosmeticRecipe;

import java.util.List;

@NullMarked
public class CosmeticRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final ItemStackTemplate result;
	private final Ingredient input;
	private final Ingredient[] reagents;
	private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
	private @Nullable String group;

	public CosmeticRecipeBuilder(RecipeCategory category, ItemStackTemplate result, Ingredient input, Ingredient... reagents) {
		this.category = category;
		this.result = result;
		this.input = input;
		this.reagents = reagents;
	}

	public static CosmeticRecipeBuilder cosmetic(
			RecipeCategory category,
			ItemLike result,
			Ingredient input,
			Ingredient... reagents
	) {
		return new CosmeticRecipeBuilder(category, new ItemStackTemplate(result.asItem(), 1), input, reagents);
	}

	@Override public CosmeticRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		this.advancementBuilder.unlockedBy(name, criterion);
		return this;
	}

	@Override public CosmeticRecipeBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	@Override public ResourceKey<Recipe<?>> defaultId() {
		return RecipeBuilder.getDefaultRecipeId(this.result);
	}

	@Override public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
		validate(id);
		CosmeticRecipe recipe = new CosmeticRecipe(
				RecipeBuilder.createCraftingCommonInfo(true),
				RecipeBuilder.createCraftingBookInfo(this.category, this.group),
				this.input,
				List.of(this.reagents),
				this.result
		);

		output.accept(id, recipe, this.advancementBuilder.build(output, id, this.category));
	}

	private void validate(ResourceKey<Recipe<?>> recipe) {
		if (this.reagents.length == 0)
			throw new IllegalStateException("No reagents specified for recipe " + recipe.identifier());
	}
}
