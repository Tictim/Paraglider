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
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;

import java.util.concurrent.CompletableFuture;

public class TestRecipeGen extends RecipeProvider {
	public TestRecipeGen(@NotNull HolderLookup.Provider registries, @NotNull RecipeOutput output) {
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
			return new TestRecipeGen(registries, output);
		}

		@Override public @NotNull String getName() {
			return "Paraglider test recipes";
		}
	}
}
