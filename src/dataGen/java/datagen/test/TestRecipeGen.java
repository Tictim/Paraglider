package datagen.test;

import datagen.builder.StatueBargainBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class TestRecipeGen extends RecipeProvider {
	public TestRecipeGen(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}

	@Override protected void buildRecipes() {
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(tag(ItemTags.LEAVES), 64)
				.demand(Items.STICK, 64)
				.demand(tag(ItemTags.LEAVES), 64)
				.offer(Items.OAK_BOAT, 1)
				.save(this.output, id("bargain_test/1"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.GOLD_INGOT, 1)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/2"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.IRON_INGOT, 2)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/3"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.GOLD_INGOT, 3)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/4"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.IRON_INGOT, 4)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/5"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.GOLD_INGOT, 5)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/6"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.IRON_INGOT, 6)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/7"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.GOLD_INGOT, 7)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/8"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.IRON_INGOT, 8)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/9"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.GOLD_INGOT, 9)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/10"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Items.IRON_INGOT, 10)
				.offer(Items.DIRT, 1)
				.save(this.output, id("bargain_test/11"));
		{
			var b = new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE);
			Blocks.COPPER_GRATE.forEach(block -> b.demand(Ingredient.of(block), 64));
			Blocks.COPPER_GRATE.forEach(block -> b.offer(block.asItem(), 64));
			b.save(this.output, id("bargain_test/things"));
		}
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Ingredient.of(Items.STICK), 1)
				.offerHeartContainer(2)
				.save(this.output, id("bargain_test/heart"));
		new StatueBargainBuilder(ParagliderBargainTypes.GODDESS_STATUE)
				.demand(Ingredient.of(Items.STICK), 1)
				.offerHeartContainer(100)
				.save(this.output, id("bargain_test/heart100"));
	}

	private static ResourceKey<Recipe<?>> id(String id) {
		return ResourceKey.create(Registries.RECIPE, ParagliderAPI.id(id));
	}

	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override protected RecipeProvider createRecipeProvider(
				HolderLookup.Provider registries, RecipeOutput output
		) {
			return new TestRecipeGen(registries, output);
		}

		@Override public String getName() {
			return "Paraglider test recipes";
		}
	}
}
