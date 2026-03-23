package datagen;

import datagen.builder.StatueBargainBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderConfigCondition;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.contents.recipe.WaterBottleIngredientType;

import java.util.concurrent.CompletableFuture;

import static datagen.builder.CosmeticRecipeBuilder.cosmetic;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.*;

public class RecipeGen extends RecipeProvider {
	public RecipeGen(@NotNull HolderLookup.Provider registries, @NotNull RecipeOutput output) {
		super(registries, output);
	}

	@Override protected void buildRecipes() {
		Contents contents = Contents.get();

		shaped(RecipeCategory.MISC, contents.paraglider())
				.pattern("121")
				.pattern("212")
				.pattern("1 1")
				.define('1', Tags.Items.RODS_WOODEN)
				.define('2', Tags.Items.LEATHERS)
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.save(this.output);

		shapeless(RecipeCategory.MISC, contents.energizingMixture())
				.requires(new Ingredient(WaterBottleIngredientType.INSTANCE))
				.requires(Items.CARROT)
				.unlockedBy("has_carrot", has(Items.CARROT))
				.save(this.output);

		shapeless(RecipeCategory.MISC, contents.enduringMixture())
				.requires(new Ingredient(WaterBottleIngredientType.INSTANCE))
				.requires(Items.GOLDEN_CARROT)
				.unlockedBy("has_golden_carrot", has(Items.GOLDEN_CARROT))
				.save(this.output);

		dyedItem(contents.paraglider(), "dyed_armor");
		dyedItem(contents.dekuLeaf(), "dyed_armor");

		smelting(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, CookingBookCategory.MISC, contents.energizingElixir1(), 0.35F, 200)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(this.output, id("smelting/energizing_elixir_1"));
		smelting(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, CookingBookCategory.MISC, contents.enduringElixir1(), 0.35F, 200)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(this.output, id("smelting/enduring_elixir_1"));

		smoking(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, contents.energizingElixir1(), 0.35F, 100)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(this.output, id("smoking/energizing_elixir_1"));
		smoking(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, contents.enduringElixir1(), 0.35F, 100)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(this.output, id("smoking/enduring_elixir_1"));

		campfireCooking(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, contents.energizingElixir1(), 0.35F, 600)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(this.output, id("campfire_cooking/energizing_elixir_1"));
		campfireCooking(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, contents.enduringElixir1(), 0.35F, 600)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(this.output, id("campfire_cooking/enduring_elixir_1"));

		cosmetic(RecipeCategory.MISC, contents.dekuLeaf(),
				tag(ParagliderTags.PARAGLIDERS), Ingredient.of(Blocks.DIRT))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(this.output, id("cosmetic/deku_leaf"));
		cosmetic(RecipeCategory.MISC, contents.paraglider(),
				tag(ParagliderTags.PARAGLIDERS), tag(Tags.Items.RODS_WOODEN))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(this.output, id("cosmetic/paraglider"));

		cosmetic(RecipeCategory.MISC, contents.goddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(Tags.Items.COBBLESTONES))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/goddess_statue"));
		cosmetic(RecipeCategory.MISC, contents.kakarikoGoddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(ItemTags.PLANKS))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/kakariko_goddess_statue"));
		cosmetic(RecipeCategory.MISC, contents.goronGoddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(Tags.Items.INGOTS_GOLD))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/goron_goddess_statue"));
		cosmetic(RecipeCategory.MISC, contents.ritoGoddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(ItemTags.FLOWERS))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/rito_goddess_statue"));

		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerHeartContainer(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(this.output, id("goddess_statue/heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerStaminaVessel(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(this.output, id("goddess_statue/stamina_vessel"));

		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(this.output, id("horned_statue/sell_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(this.output, id("horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(tag(Tags.Items.GEMS_EMERALD), 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(this.output, id("horned_statue/buy_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(tag(Tags.Items.GEMS_EMERALD), 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(this.output, id("horned_statue/buy_stamina_vessel"));
	}

	private static ResourceKey<Recipe<?>> id(String id) {
		return ResourceKey.create(Registries.RECIPE, ParagliderAPI.id(id));
	}

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override protected @NotNull RecipeProvider createRecipeProvider(
				HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output
		) {
			return new RecipeGen(registries, output);
		}

		@Override public @NotNull String getName() {
			return "Paraglider recipes";
		}
	}
}
