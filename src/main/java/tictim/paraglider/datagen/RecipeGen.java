package tictim.paraglider.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import java.util.function.Consumer;

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

		ShapelessRecipeBuilder.shapelessRecipe(Contents.DEKU_LEAF.get())
				.addIngredient(Contents.PARAGLIDER.get())
				.addIngredient(ItemTags.LEAVES)
				.addCriterion("has_paragliders", hasItem(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(ParagliderMod.MODID, "convert_deku_leaf"));
		ShapelessRecipeBuilder.shapelessRecipe(Contents.PARAGLIDER.get())
				.addIngredient(Contents.DEKU_LEAF.get())
				.addIngredient(Tags.Items.RODS_WOODEN)
				.addCriterion("has_paragliders", hasItem(ModTags.PARAGLIDERS))
				.build(consumer, new ResourceLocation(ParagliderMod.MODID, "convert_paraglider"));
	}
}
