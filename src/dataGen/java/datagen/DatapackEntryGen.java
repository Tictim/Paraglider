package datagen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.core.registries.Registries.*;

public class DatapackEntryGen extends DatapackBuiltinEntriesProvider {
	public DatapackEntryGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, new RegistrySetBuilder()
						.add(STRUCTURE, StructureGen::register)
						.add(STRUCTURE_SET, StructureSetGen::register)
						.add(TEMPLATE_POOL, TemplatePoolGen::register),
				Set.of(ParagliderAPI.MODID));
	}

	private static final class StructureGen {
		public static final ResourceKey<Structure> NETHER_HORNED_STATUE = ResourceKey.create(
				STRUCTURE, ParagliderAPI.id("nether_horned_statue"));
		public static final ResourceKey<Structure> TARREY_TOWN_GODDESS_STATUE = ResourceKey.create(
				STRUCTURE, ParagliderAPI.id("tarrey_town_goddess_statue"));
		public static final ResourceKey<Structure> UNDERGROUND_HORNED_STATUE = ResourceKey.create(
				STRUCTURE, ParagliderAPI.id("underground_horned_statue"));

		private static void register(BootstrapContext<Structure> context) {
			HolderGetter<Biome> biomes = context.lookup(BIOME);

			context.register(NETHER_HORNED_STATUE, new NetherHornedStatue(
					new Structure.StructureSettings(biomes.getOrThrow(ParagliderTags.Biomes.HAS_STRUCTURE_NETHER_HORNED_STATUE)),
					UniformHeight.of(new VerticalAnchor.Absolute(32), new VerticalAnchor.BelowTop(2))
			));

			context.register(TARREY_TOWN_GODDESS_STATUE, new TarreyTownGoddessStatue(
					new Structure.StructureSettings(biomes.getOrThrow(ParagliderTags.Biomes.HAS_STRUCTURE_TARREY_TOWN_GODDESS_STATUE))
			));

			context.register(UNDERGROUND_HORNED_STATUE, new UndergroundHornedStatue(
					new Structure.StructureSettings(biomes.getOrThrow(ParagliderTags.Biomes.HAS_STRUCTURE_UNDERGROUND_HORNED_STATUE))
			));
		}
	}

	private static final class StructureSetGen {
		public static final ResourceKey<StructureSet> NETHER_HORNED_STATUE = ResourceKey.create(
				STRUCTURE_SET, ParagliderAPI.id("nether_horned_statue"));
		public static final ResourceKey<StructureSet> TARREY_TOWN_GODDESS_STATUE = ResourceKey.create(
				STRUCTURE_SET, ParagliderAPI.id("tarrey_town_goddess_statue"));
		public static final ResourceKey<StructureSet> UNDERGROUND_HORNED_STATUE = ResourceKey.create(
				STRUCTURE_SET, ParagliderAPI.id("underground_horned_statue"));

		private static void register(BootstrapContext<StructureSet> context) {
			HolderGetter<Structure> structures = context.lookup(STRUCTURE);

			context.register(NETHER_HORNED_STATUE, new StructureSet(
					structures.getOrThrow(StructureGen.NETHER_HORNED_STATUE),
					new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 1973135446)
			));

			context.register(TARREY_TOWN_GODDESS_STATUE, new StructureSet(
					structures.getOrThrow(StructureGen.TARREY_TOWN_GODDESS_STATUE),
					new RandomSpreadStructurePlacement(128, 16, RandomSpreadType.LINEAR, 850796625)
			));

			context.register(UNDERGROUND_HORNED_STATUE, new StructureSet(
					structures.getOrThrow(StructureGen.UNDERGROUND_HORNED_STATUE),
					new RandomSpreadStructurePlacement(16, 4, RandomSpreadType.LINEAR, 49788929)
			));
		}
	}

	private static final class TemplatePoolGen {
		public static final ResourceKey<StructureTemplatePool> HORNED_STATUE = ResourceKey.create(
				TEMPLATE_POOL, ParagliderAPI.id("horned_statue"));

		private static void register(BootstrapContext<StructureTemplatePool> context) {
			context.register(HORNED_STATUE, new StructureTemplatePool(
					context.lookup(TEMPLATE_POOL).getOrThrow(ResourceKey.create(TEMPLATE_POOL, Identifier.withDefaultNamespace("empty"))),
					List.of(
							Pair.of(StructurePoolElement.single("paraglider:horned_statue/yes"), 1),
							Pair.of(StructurePoolElement.single("paraglider:horned_statue/no"), 5)
					),
					StructureTemplatePool.Projection.RIGID
			));
		}
	}
}
