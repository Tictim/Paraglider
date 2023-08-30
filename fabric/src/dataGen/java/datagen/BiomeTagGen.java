package datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

public class BiomeTagGen extends BiomeTagsProvider{
	public BiomeTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
		super(output, registriesFuture);
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		// Identical to mineshaft
		tag(ParagliderTags.Biomes.HAS_STRUCTURE_UNDERGROUND_HORNED_STATUE)
				.addOptionalTag(BiomeTags.IS_OCEAN.location())
				.addOptionalTag(BiomeTags.IS_RIVER.location())
				.addOptionalTag(BiomeTags.IS_BEACH.location())
				.addOptionalTag(BiomeTags.IS_MOUNTAIN.location())
				.addOptionalTag(BiomeTags.IS_HILL.location())
				.addOptionalTag(BiomeTags.IS_TAIGA.location())
				.addOptionalTag(BiomeTags.IS_JUNGLE.location())
				.addOptionalTag(BiomeTags.IS_FOREST.location())
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

		tag(ParagliderTags.Biomes.HAS_STRUCTURE_NETHER_HORNED_STATUE)
				.add(Biomes.NETHER_WASTES)
				.add(Biomes.SOUL_SAND_VALLEY)
				.add(Biomes.WARPED_FOREST);

		tag(ParagliderTags.Biomes.HAS_STRUCTURE_TARREY_TOWN_GODDESS_STATUE)
				.add(Biomes.PLAINS)
				.add(Biomes.MEADOW);
	}
}
