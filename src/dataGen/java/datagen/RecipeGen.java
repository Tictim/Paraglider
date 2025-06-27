package datagen;

import datagen.builder.CosmeticRecipeBuilder;
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

import java.util.concurrent.CompletableFuture;

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

		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.dekuLeaf(),
				tag(ParagliderTags.PARAGLIDERS), Ingredient.of(Blocks.DIRT))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(this.output, id("cosmetic/deku_leaf"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.paraglider(),
				tag(ParagliderTags.PARAGLIDERS), tag(Tags.Items.RODS_WOODEN))
				.unlockedBy("has_paragliders", has(ParagliderTags.PARAGLIDERS))
				.save(this.output, id("cosmetic/paraglider"));

		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.goddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(Tags.Items.COBBLESTONES))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.kakarikoGoddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(ItemTags.PLANKS))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/kakariko_goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.goronGoddessStatueItem(),
				tag(ParagliderTags.STATUES_GODDESS), tag(Tags.Items.INGOTS_GOLD))
				.unlockedBy("has_goddess_statue", has(ParagliderTags.STATUES_GODDESS))
				.save(this.output, id("cosmetic/goron_goddess_statue"));
		new CosmeticRecipeBuilder(RecipeCategory.MISC, contents.ritoGoddessStatueItem(),
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
