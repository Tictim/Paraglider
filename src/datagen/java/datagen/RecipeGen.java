package datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;
import datagen.builder.CosmeticRecipeBuilder;
import datagen.builder.StatueBargainBuilder;
import tictim.paraglider.contents.recipe.ConfigConditionSerializer;

import java.util.function.Consumer;

import static tictim.paraglider.ParagliderMod.MODID;

public class RecipeGen extends RecipeProvider{
	public RecipeGen(DataGenerator generatorIn){
		super(generatorIn);
	}

	@Override protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer){
		ShapedRecipeBuilder.shaped(Contents.PARAGLIDER.get())
				.pattern("121")
				.pattern("212")
				.pattern("1 1")
				.define('1', Tags.Items.RODS_WOODEN)
				.define('2', Tags.Items.LEATHER)
				.unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
				.save(consumer);

		new CosmeticRecipeBuilder(Contents.DEKU_LEAF.get(), Ingredient.of(ModTags.PARAGLIDERS), Ingredient.of(ItemTags.LEAVES))
				.addCriterion("has_paragliders", has(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(Contents.PARAGLIDER.get(), Ingredient.of(ModTags.PARAGLIDERS), Ingredient.of(Tags.Items.RODS_WOODEN))
				.addCriterion("has_paragliders", has(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/paraglider"));

		new CosmeticRecipeBuilder(Contents.GODDESS_STATUE_ITEM.get(), Ingredient.of(ModTags.STATUES_GODDESS), Ingredient.of(Tags.Items.COBBLESTONE))
				.addCriterion("has_goddess_statue", has(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(Contents.KAKARIKO_GODDESS_STATUE_ITEM.get(), Ingredient.of(ModTags.STATUES_GODDESS), Ingredient.of(ItemTags.PLANKS))
				.addCriterion("has_goddess_statue", has(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(Contents.GORON_GODDESS_STATUE_ITEM.get(), Ingredient.of(ModTags.STATUES_GODDESS), Ingredient.of(Tags.Items.INGOTS_GOLD))
				.addCriterion("has_goddess_statue", has(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(Contents.RITO_GODDESS_STATUE_ITEM.get(), Ingredient.of(ModTags.STATUES_GODDESS), Ingredient.of(ItemTags.FLOWERS))
				.addCriterion("has_goddess_statue", has(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/rito_goddess_statue"));

		ResourceLocation goddessStatue = Contents.GODDESS_STATUE_CONTAINER.getId();
		new StatueBargainBuilder(goddessStatue)
				.demand(Contents.SPIRIT_ORB.get(), 4)
				.offerHeartContainer(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "goddess_statue/heart_container"));
		new StatueBargainBuilder(goddessStatue)
				.demand(Contents.SPIRIT_ORB.get(), 4)
				.offerStaminaVessel(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "goddess_statue/stamina_vessel"));

		ResourceLocation hornedStatue = Contents.HORNED_STATUE_CONTAINER.getId();
		new StatueBargainBuilder(hornedStatue)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "horned_statue/sell_heart_container"));
		new StatueBargainBuilder(hornedStatue)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.condition(ConfigConditionSerializer.HEART_CONTAINER_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "horned_statue/buy_heart_container"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.condition(ConfigConditionSerializer.STAMINA_VESSEL_ENABLED.create())
				.build(consumer, new ResourceLocation(MODID, "horned_statue/buy_stamina_vessel"));
	}
}
