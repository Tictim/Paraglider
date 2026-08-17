package datagen.builder;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.contents.recipe.SimpleBargain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NullMarked
public class StatueBargainBuilder implements RecipeBuilder {
	protected final Identifier bargainType;

	protected final List<SizedIngredient> itemDemands = new ArrayList<>();
	protected int heartContainerDemands;
	protected int staminaVesselDemands;
	protected int essenceDemands;

	protected final List<ItemStackTemplate> itemOffers = new ArrayList<>();
	protected int heartContainerOffers;
	protected int staminaVesselOffers;
	protected int essenceOffers;

	protected final List<ICondition> conditions = new ArrayList<>();

	public StatueBargainBuilder(Identifier bargainType) {
		this.bargainType = Objects.requireNonNull(bargainType);
	}

	public StatueBargainBuilder demand(ItemLike item, int quantity) {
		return demand(Ingredient.of(item), quantity);
	}

	public StatueBargainBuilder demand(Ingredient ingredient, int quantity) {
		this.itemDemands.add(new SizedIngredient(ingredient, quantity));
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
		this.itemOffers.add(new ItemStackTemplate(item, count));
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

	@Override public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		return this;
	}
	@Override public RecipeBuilder group(@Nullable String groupName) {
		return this;
	}

	@Override public ResourceKey<Recipe<?>> defaultId() {
		throw new UnsupportedOperationException("Specify recipe ID");
	}

	@Override public void save(RecipeOutput output, ResourceKey<Recipe<?>> resourceKey) {
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

	@Override public void save(RecipeOutput recipeOutput) {
		throw new UnsupportedOperationException("Specify recipe ID");
	}
}
