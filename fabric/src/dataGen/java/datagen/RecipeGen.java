package datagen;

import datagen.builder.CosmeticRecipeBuilder;
import datagen.builder.StatueBargainBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.function.Consumer;

public class RecipeGen extends FabricRecipeProvider{
	public RecipeGen(FabricDataOutput output){
		super(output);
	}

	@Override public void buildRecipes(Consumer<FinishedRecipe> consumer){
		Contents contents = Contents.get();

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, contents.paraglider())
				.pattern("121")
				.pattern("212")
				.pattern("1 1")
				.define('1', Items.STICK)
				.define('2', Items.LEATHER)
				.unlockedBy("has_stick", has(Items.STICK))
				.save(consumer);

		new CosmeticRecipeBuilder(contents.dekuLeaf(), Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(ItemTags.LEAVES))
				.addCriterion("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.build(consumer, ParagliderAPI.id("cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(contents.paraglider(), Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(Items.STICK))
				.addCriterion("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.build(consumer, ParagliderAPI.id("cosmetic/paraglider"));

		new CosmeticRecipeBuilder(contents.goddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Blocks.COBBLESTONE))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.build(consumer, ParagliderAPI.id("cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(contents.kakarikoGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.PLANKS))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.build(consumer, ParagliderAPI.id("cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(contents.goronGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Items.GOLD_INGOT))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.build(consumer, ParagliderAPI.id("cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(contents.ritoGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.FLOWERS))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.build(consumer, ParagliderAPI.id("cosmetic/rito_goddess_statue"));

		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerHeartContainer(1)
				.build(consumer, ParagliderAPI.id("goddess_statue/heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerStaminaVessel(1)
				.build(consumer, ParagliderAPI.id("goddess_statue/stamina_vessel"));

		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.build(consumer, ParagliderAPI.id("horned_statue/sell_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.build(consumer, ParagliderAPI.id("horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Items.EMERALD, 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.build(consumer, ParagliderAPI.id("horned_statue/buy_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Items.EMERALD, 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.build(consumer, ParagliderAPI.id("horned_statue/buy_stamina_vessel"));
	}
}
