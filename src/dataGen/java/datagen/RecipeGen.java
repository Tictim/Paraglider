package datagen;

import datagen.builder.CosmeticRecipeBuilder;
import datagen.builder.StatueBargainBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderConfigCondition;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.contents.recipe.WaterBottleIngredientType;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.data.recipes.ShapedRecipeBuilder.shaped;
import static net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.*;
import static tictim.paraglider.api.ParagliderAPI.id;

public class RecipeGen extends RecipeProvider {
	public RecipeGen(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override protected void buildRecipes(@NotNull RecipeOutput output) {
		Contents contents = Contents.get();

		shaped(RecipeCategory.MISC, contents.paraglider())
				.pattern("121")
				.pattern("212")
				.pattern("1 1")
				.define('1', Tags.Items.RODS_WOODEN)
				.define('2', Tags.Items.LEATHERS)
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.save(output);

		shapeless(RecipeCategory.MISC, contents.energizingMixture())
				.requires(new Ingredient(WaterBottleIngredientType.INSTANCE))
				.requires(Items.CARROT)
				.unlockedBy("has_carrot", has(Items.CARROT))
				.save(output);

		shapeless(RecipeCategory.MISC, contents.enduringMixture())
				.requires(new Ingredient(WaterBottleIngredientType.INSTANCE))
				.requires(Items.GOLDEN_CARROT)
				.unlockedBy("has_golden_carrot", has(Items.GOLDEN_CARROT))
				.save(output);

		smelting(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, contents.energizingElixir1(), 0.35F, 200)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(output, id("smelting/energizing_elixir_1"));
		smelting(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, contents.enduringElixir1(), 0.35F, 200)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(output, id("smelting/enduring_elixir_1"));

		smoking(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, contents.energizingElixir1(), 0.35F, 100)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(output, id("smoking/energizing_elixir_1"));
		smoking(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, contents.enduringElixir1(), 0.35F, 100)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(output, id("smoking/enduring_elixir_1"));

		campfireCooking(Ingredient.of(contents.energizingMixture()), RecipeCategory.MISC, contents.energizingElixir1(), 0.35F, 600)
				.unlockedBy("has_energizing_mixture", has(contents.energizingMixture()))
				.save(output, id("campfire_cooking/energizing_elixir_1"));
		campfireCooking(Ingredient.of(contents.enduringMixture()), RecipeCategory.MISC, contents.enduringElixir1(), 0.35F, 600)
				.unlockedBy("has_enduring_mixture", has(contents.enduringMixture()))
				.save(output, id("campfire_cooking/enduring_elixir_1"));

		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.dekuLeaf(),
				Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(Blocks.DIRT))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(output, id("cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.paraglider(),
				Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(Tags.Items.RODS_WOODEN))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(output, id("cosmetic/paraglider"));

		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.goddessStatueItem(),
				Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Tags.Items.COBBLESTONES))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(output, id("cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.kakarikoGoddessStatueItem(),
				Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.PLANKS))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(output, id("cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.goronGoddessStatueItem(),
				Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Tags.Items.INGOTS_GOLD))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(output, id("cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.ritoGoddessStatueItem(),
				Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.FLOWERS))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(output, id("cosmetic/rito_goddess_statue"));

		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerHeartContainer(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(output, id("goddess_statue/heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerStaminaVessel(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(output, id("goddess_statue/stamina_vessel"));

		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(output, id("horned_statue/sell_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(output, id("horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Ingredient.of(Tags.Items.GEMS_EMERALD), 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.condition(ParagliderConfigCondition.HEART_CONTAINER_ENABLED)
				.save(output, id("horned_statue/buy_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Ingredient.of(Tags.Items.GEMS_EMERALD), 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.condition(ParagliderConfigCondition.STAMINA_VESSEL_ENABLED)
				.save(output, id("horned_statue/buy_stamina_vessel"));
	}
}
