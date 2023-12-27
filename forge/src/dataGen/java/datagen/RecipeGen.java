package datagen;

import datagen.builder.CosmeticRecipeBuilder;
import datagen.builder.StatueBargainBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.forge.contents.ConfigConditionSerializer;

public class RecipeGen extends RecipeProvider{
	public RecipeGen(@NotNull PackOutput output){
		super(output);
	}

	@Override protected void buildRecipes(@NotNull RecipeOutput recipeOutput){
		Contents contents = Contents.get();

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, contents.paraglider())
				.pattern("121")
				.pattern("212")
				.pattern("1 1")
				.define('1', Tags.Items.RODS_WOODEN)
				.define('2', Tags.Items.LEATHER)
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.save(recipeOutput);

		new CosmeticRecipeBuilder(contents.dekuLeaf(), Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(ItemTags.LEAVES))
				.addCriterion("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(contents.paraglider(), Ingredient.of(ParagliderTags.PARAGLIDERS), Ingredient.of(Tags.Items.RODS_WOODEN))
				.addCriterion("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/paraglider"));

		new CosmeticRecipeBuilder(contents.goddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Tags.Items.COBBLESTONE))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(contents.kakarikoGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.PLANKS))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(contents.goronGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(Tags.Items.INGOTS_GOLD))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(contents.ritoGoddessStatueItem(), Ingredient.of(ParagliderTags.STATUES_GODDESS), Ingredient.of(ItemTags.FLOWERS))
				.addCriterion("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(recipeOutput, ParagliderAPI.id("cosmetic/rito_goddess_statue"));

		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerHeartContainer(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("goddess_statue/heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(contents.spiritOrb(), 4)
				.offerStaminaVessel(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("goddess_statue/stamina_vessel"));

		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("horned_statue/sell_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("horned_statue/buy_heart_container"));
		new StatueBargainBuilder(ParagliderBargainTypes.HORNED_STATUE)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED)
				.save(recipeOutput, ParagliderAPI.id("horned_statue/buy_stamina_vessel"));
	}
}
