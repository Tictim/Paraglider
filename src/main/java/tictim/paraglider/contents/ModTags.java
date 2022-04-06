package tictim.paraglider.contents;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.registries.RegistryObject;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModTags{
	private ModTags(){}

	public static final TagKey<Item> PARAGLIDERS = ItemTags.create(new ResourceLocation(MODID, "paragliders"));
	public static final TagKey<Item> STATUES = ItemTags.create(new ResourceLocation(MODID, "statues"));
	public static final TagKey<Item> STATUES_GODDESS = ItemTags.create(new ResourceLocation(MODID, "statues/goddess"));

	public static final class Blocks{
		private Blocks(){}

		public static final TagKey<Block> STATUES = BlockTags.create(new ResourceLocation(MODID, "statues"));
		public static final TagKey<Block> STATUES_GODDESS = BlockTags.create(new ResourceLocation(MODID, "statues/goddess"));
	}

	public static final class Biomes{
		public static final TagKey<Biome> HAS_STRUCTURE_UNDERGROUND_HORNED_STATUE = hasStructure(Contents.UNDERGROUND_HORNED_STATUE);
		public static final TagKey<Biome> HAS_STRUCTURE_NETHER_HORNED_STATUE = hasStructure(Contents.NETHER_HORNED_STATUE);
		public static final TagKey<Biome> HAS_STRUCTURE_TARREY_TOWN_GODDESS_STATUE = hasStructure(Contents.TARREY_TOWN_GODDESS_STATUE);

		private static TagKey<Biome> hasStructure(RegistryObject<StructureFeature<?>> o){
			ResourceLocation id = o.getId();
			return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(id.getNamespace(), "has_structure/"+id.getPath()));
		}
	}
}
