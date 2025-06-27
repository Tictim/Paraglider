package datagen.builder;

import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.contents.recipe.QuantifiedIngredient;
import tictim.paraglider.contents.recipe.QuantifiedItem;
import tictim.paraglider.contents.recipe.SimpleBargain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class StatueBargainBuilder implements RecipeBuilder {
	protected final ResourceLocation bargainType;

	protected final List<QuantifiedIngredient> itemDemands = new ArrayList<>();
	protected int heartContainerDemands;
	protected int staminaVesselDemands;
	protected int essenceDemands;

	protected final List<QuantifiedItem> itemOffers = new ArrayList<>();
	protected int heartContainerOffers;
	protected int staminaVesselOffers;
	protected int essenceOffers;

	protected final List<ICondition> conditions = new ArrayList<>();

	public StatueBargainBuilder(ResourceLocation bargainType) {
		this.bargainType = Objects.requireNonNull(bargainType);
	}

	public StatueBargainBuilder demand(ItemLike item, int quantity) {
		return demand(Ingredient.of(item), quantity);
	}

	public StatueBargainBuilder demand(Ingredient ingredient, int quantity) {
		this.itemDemands.add(new QuantifiedIngredient(ingredient, quantity));
		return this;
	}

	public StatueBargainBuilder demandHeartContainer(int quantity) {
		this.heartContainerDemands = quantity;
		return this;
	}

	public StatueBargainBuilder demandStaminaVessel(int quantity) {
		this.staminaVesselDemands = quantity;
		return this;
	}

	public StatueBargainBuilder demandEssence(int quantity) {
		this.essenceDemands = quantity;
		return this;
	}

	public StatueBargainBuilder offer(Item item, int count) {
		this.itemOffers.add(new QuantifiedItem(item, count));
		return this;
	}

	public StatueBargainBuilder offerHeartContainer(int quantity) {
		this.heartContainerOffers = quantity;
		return this;
	}

	public StatueBargainBuilder offerStaminaVessel(int quantity) {
		this.staminaVesselOffers = quantity;
		return this;
	}

	public StatueBargainBuilder offerEssence(int quantity) {
		this.essenceOffers = quantity;
		return this;
	}

	public StatueBargainBuilder condition(ICondition condition) {
		this.conditions.add(Objects.requireNonNull(condition));
		return this;
	}

	@Override public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
		return this;
	}
	@Override public @NotNull RecipeBuilder group(@Nullable String groupName) {
		return this;
	}
	@Override public @NotNull Item getResult() {
		return Items.AIR;
	}

	@Override public void save(RecipeOutput output, @NotNull ResourceKey<Recipe<?>> resourceKey) {
		output.accept(resourceKey, new SimpleBargain(
						this.bargainType,
						this.itemDemands,
						this.heartContainerDemands,
						this.staminaVesselDemands,
						this.essenceDemands,
						this.itemOffers,
						this.heartContainerOffers,
						this.staminaVesselOffers,
						this.essenceOffers, Set.of()
				),
				null,
				this.conditions.toArray(new ICondition[0]));
	}

	@Override public void save(@NotNull RecipeOutput recipeOutput) {
		throw new UnsupportedOperationException("Specify recipe ID");
	}
}
