package datagen.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.recipe.CosmeticRecipe;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CosmeticRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	private final Ingredient input;
	private final Ingredient[] reagents;
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
	private String group;

	public CosmeticRecipeBuilder(RecipeCategory category, Item result, Ingredient input, Ingredient... reagents) {
		this.category = category;
		this.result = result;
		this.input = input;
		this.reagents = reagents;
	}

	@Override public @NotNull Item getResult() {
		return result;
	}

	@Override public @NotNull CosmeticRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
		this.criteria.put(name, criterion);
		return this;
	}

	@Override public @NotNull CosmeticRecipeBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	@Override public void save(@NotNull RecipeOutput output, @NotNull ResourceKey<@NotNull Recipe<?>> resourceKey) {
		validate(resourceKey);

		Advancement.Builder a = output.advancement()
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
				.rewards(AdvancementRewards.Builder.recipe(resourceKey))
				.requirements(AdvancementRequirements.Strategy.OR);

		this.criteria.forEach(a::addCriterion);

		CosmeticRecipe r = new CosmeticRecipe(
				Objects.requireNonNullElse(this.group, ""),
				RecipeBuilder.determineBookCategory(this.category),
				this.input,
				List.of(this.reagents),
				new TransmuteResult(this.result)
		);

		output.accept(resourceKey, r, a.build(resourceKey.identifier()
				.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void validate(ResourceKey<@NotNull Recipe<?>> recipe) {
		if (this.criteria.isEmpty())
			throw new IllegalStateException("No way of obtaining recipe " + recipe.identifier());
		if (this.reagents.length == 0)
			throw new IllegalStateException("No reagents specified for recipe " + recipe.identifier());
	}
}
