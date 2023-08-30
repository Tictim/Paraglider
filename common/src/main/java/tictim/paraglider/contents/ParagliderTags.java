package tictim.paraglider.contents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.core.registries.Registries.BLOCK;
import static net.minecraft.core.registries.Registries.ITEM;
import static tictim.paraglider.api.ParagliderAPI.id;

public interface ParagliderTags{
	TagKey<Item> PARAGLIDERS = TagKey.create(ITEM, id("paragliders"));
	TagKey<Item> STATUES = TagKey.create(ITEM, id("statues"));
	TagKey<Item> STATUES_GODDESS = TagKey.create(ITEM, id("statues/goddess"));

	interface Blocks{
		TagKey<Block> STATUES = TagKey.create(BLOCK, id("statues"));
		TagKey<Block> STATUES_GODDESS = TagKey.create(BLOCK, id("statues/goddess"));
	}

	interface Biomes{
		TagKey<Biome> HAS_STRUCTURE_UNDERGROUND_HORNED_STATUE = hasStructure(id("underground_horned_statue"));
		TagKey<Biome> HAS_STRUCTURE_NETHER_HORNED_STATUE = hasStructure(id("nether_horned_statue"));
		TagKey<Biome> HAS_STRUCTURE_TARREY_TOWN_GODDESS_STATUE = hasStructure(id("tarrey_town_goddess_statue"));

		@NotNull private static TagKey<Biome> hasStructure(@NotNull ResourceLocation id){
			return TagKey.create(Registries.BIOME, id.withPrefix("has_structure/"));
		}
	}
}
