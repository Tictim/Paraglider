package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BiomeTagGen extends BiomeTagsProvider{
	public BiomeTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper){
		super(output, lookupProvider, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		// Identical to mineshaft
		tag(ModTags.Biomes.HAS_STRUCTURE_UNDERGROUND_HORNED_STATUE)
				.addTag(BiomeTags.IS_OCEAN)
				.addTag(BiomeTags.IS_RIVER)
				.addTag(BiomeTags.IS_BEACH)
				.addTag(BiomeTags.IS_MOUNTAIN)
				.addTag(BiomeTags.IS_HILL)
				.addTag(BiomeTags.IS_TAIGA)
				.addTag(BiomeTags.IS_JUNGLE)
				.addTag(BiomeTags.IS_FOREST)
				.add(Biomes.STONY_SHORE)
				.add(Biomes.MUSHROOM_FIELDS)
				.add(Biomes.ICE_SPIKES)
				.add(Biomes.WINDSWEPT_SAVANNA)
				.add(Biomes.DESERT)
				.add(Biomes.SAVANNA)
				.add(Biomes.SNOWY_PLAINS)
				.add(Biomes.PLAINS)
				.add(Biomes.SUNFLOWER_PLAINS)
				.add(Biomes.SWAMP)
				.add(Biomes.SAVANNA_PLATEAU)
				.add(Biomes.DRIPSTONE_CAVES)
				.add(Biomes.LUSH_CAVES);

		tag(ModTags.Biomes.HAS_STRUCTURE_NETHER_HORNED_STATUE)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.WARPED_FOREST);

		tag(ModTags.Biomes.HAS_STRUCTURE_TARREY_TOWN_GODDESS_STATUE)
				.add(Biomes.PLAINS)
				.add(Biomes.MEADOW);
	}
}
