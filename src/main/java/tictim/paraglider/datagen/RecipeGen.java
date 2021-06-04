package tictim.paraglider.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import java.util.function.Consumer;

import static tictim.paraglider.ParagliderMod.MODID;

public class RecipeGen extends RecipeProvider{
	public RecipeGen(DataGenerator generatorIn){
		super(generatorIn);
	}

	@Override protected void registerRecipes(Consumer<IFinishedRecipe> consumer){
		ShapedRecipeBuilder.shapedRecipe(Contents.PARAGLIDER.get())
				.patternLine("121")
				.patternLine("212")
				.patternLine("1 1")
				.key('1', Tags.Items.RODS_WOODEN)
				.key('2', Tags.Items.LEATHER)
				.addCriterion("has_stick", hasItem(Tags.Items.RODS_WOODEN))
				.build(consumer);

		new CosmeticRecipeBuilder(Contents.DEKU_LEAF.get(), Ingredient.fromTag(ModTags.PARAGLIDERS), Ingredient.fromTag(ItemTags.LEAVES))
				.addCriterion("has_paragliders", hasItem(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(Contents.PARAGLIDER.get(), Ingredient.fromTag(ModTags.PARAGLIDERS), Ingredient.fromTag(Tags.Items.RODS_WOODEN))
				.addCriterion("has_paragliders", hasItem(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/paraglider"));

		new CosmeticRecipeBuilder(Contents.GODDESS_STATUE_ITEM.get(), Ingredient.fromTag(ModTags.STATUES_GODDESS), Ingredient.fromTag(Tags.Items.COBBLESTONE))
				.addCriterion("has_goddess_statue", hasItem(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(Contents.KAKARIKO_GODDESS_STATUE_ITEM.get(), Ingredient.fromTag(ModTags.STATUES_GODDESS), Ingredient.fromTag(ItemTags.PLANKS))
				.addCriterion("has_goddess_statue", hasItem(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(Contents.GORON_GODDESS_STATUE_ITEM.get(), Ingredient.fromTag(ModTags.STATUES_GODDESS), Ingredient.fromTag(Tags.Items.INGOTS_GOLD))
				.addCriterion("has_goddess_statue", hasItem(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(Contents.RITO_GODDESS_STATUE_ITEM.get(), Ingredient.fromTag(ModTags.STATUES_GODDESS), Ingredient.fromTag(ItemTags.FLOWERS))
				.addCriterion("has_goddess_statue", hasItem(ModTags.STATUES_GODDESS))
				.build(consumer, new ResourceLocation(MODID, "cosmetic/rito_goddess_statue"));

		ResourceLocation goddessStatue = Contents.GODDESS_STATUE_CONTAINER.getId();
		new StatueBargainBuilder(goddessStatue)
				.demand(Contents.SPIRIT_ORB.get(), 4)
				.offerHeartContainer(1)
				.build(consumer, new ResourceLocation(MODID, "goddess_statue/heart_container"));
		new StatueBargainBuilder(goddessStatue)
				.demand(Contents.SPIRIT_ORB.get(), 4)
				.offerStaminaVessel(1)
				.build(consumer, new ResourceLocation(MODID, "goddess_statue/stamina_vessel"));

		ResourceLocation hornedStatue = Contents.HORNED_STATUE_CONTAINER.getId();
		new StatueBargainBuilder(hornedStatue)
				.demandHeartContainer(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.build(consumer, new ResourceLocation(MODID, "horned_statue/sell_heart_container"));
		new StatueBargainBuilder(hornedStatue)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offerEssence(1)
				.build(consumer, new ResourceLocation(MODID, "horned_statue/sell_stamina_vessel"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerHeartContainer(1)
				.build(consumer, new ResourceLocation(MODID, "horned_statue/buy_heart_container"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Tags.Items.GEMS_EMERALD, 6)
				.demandEssence(1)
				.offerStaminaVessel(1)
				.build(consumer, new ResourceLocation(MODID, "horned_statue/buy_stamina_vessel"));

		/*
		new StatueBargainBuilder(hornedStatue)
				.demandStaminaVessel(1)
				.offer(Items.EMERALD, 5)
				.offer(Items.BOWL, 1)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/lol"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.EMERALD, 1)
				.demandHeartContainer(1)
				.demandStaminaVessel(1)
				.demandEssence(1)
				.offer(Items.COBBLESTONE, 64)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/input_overload"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.EMERALD, 1)
				.offer(Items.ACACIA_LEAVES, 64)
				.offer(Items.BIRCH_LEAVES, 64)
				.offer(Items.DARK_OAK_LEAVES, 64)
				.offer(Items.JUNGLE_LEAVES, 64)
				.offer(Items.OAK_LEAVES, 64)
				.offer(Items.SPRUCE_LEAVES, 64)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/output_overload"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.ACACIA_LEAVES, 64)
				.demand(Items.BIRCH_LEAVES, 64)
				.demand(Items.DARK_OAK_LEAVES, 64)
				.demand(Items.JUNGLE_LEAVES, 64)
				.demand(Items.OAK_LEAVES, 64)
				.demand(Items.SPRUCE_LEAVES, 64)
				.offer(Items.ACACIA_LEAVES, 64)
				.offer(Items.BIRCH_LEAVES, 64)
				.offer(Items.DARK_OAK_LEAVES, 64)
				.offer(Items.JUNGLE_LEAVES, 64)
				.offer(Items.OAK_LEAVES, 64)
				.offer(Items.SPRUCE_LEAVES, 64)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/both_overload"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.DIAMOND, 1)
				.demand(Items.DIAMOND, 1)
				.demand(Items.DIAMOND, 1)
				.demand(Items.DIAMOND, 1)
				.demand(Items.DIAMOND, 1)
				.offer(Items.DIAMOND, 5)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/multiple_input_test_1"));
		new StatueBargainBuilder(hornedStatue)
				.demandHeartContainer(1)
				.demandHeartContainer(1)
				.demandHeartContainer(1)
				.demandHeartContainer(1)
				.demandHeartContainer(1)
				.offerHeartContainer(5)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/fuck"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.DIAMOND, 1)
				.demand(Items.DIAMOND, 2)
				.demand(Items.DIAMOND, 3)
				.demand(Items.DIAMOND, 4)
				.demand(Items.DIAMOND, 5)
				.offer(Items.DIAMOND, 15)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/multiple_input_test_2"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Tags.Items.GEMS, 1)
				.demand(ItemTags.LEAVES, 1)
				.offer(Items.BOWL, 1)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/tagtest"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.BOWL, 500)
				.offer(Items.POTATO, 1)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/gazillion_inputs"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.EMERALD, 1)
				.offer(Items.BOWL, 500)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/gazillion_outputs"));
		new StatueBargainBuilder(hornedStatue)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.demand(Items.EMERALD, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.offer(Items.BOWL, 1)
				.build(consumer, new ResourceLocation(MODID, "bargain_test/fuckit"));
				*/
	}
}
