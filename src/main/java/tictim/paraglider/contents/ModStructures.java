package tictim.paraglider.contents;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import tictim.paraglider.contents.worldgen.NetherHornedStatuePiece;
import tictim.paraglider.contents.worldgen.NetherHornedStatueStructure;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatuePiece;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatueStructure;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatuePiece;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatueStructure;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ModStructures{
	private ModStructures(){}

	public static final UndergroundHornedStatueStructure UNDERGROUND_HORNED_STATUE = new UndergroundHornedStatueStructure();
	public static final IStructurePieceType UNDERGROUND_HORNED_STATUE_PIECE_TYPE = UndergroundHornedStatuePiece::new;
	public static final StructureFeature<?, ?> UNDERGROUND_HORNED_STATUE_CONFIGURED = UNDERGROUND_HORNED_STATUE.withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG);
	public static final StructureSeparationSettings UNDERGROUND_HORNED_STATUE_SEPARATION_SETTINGS = new StructureSeparationSettings(
			32, // spacing
			8, // separation
			49788929 // chosen by fair nextInt(Integer.MAX_VALUE) roll.
			// guaranteed to be random.
	);

	public static final NetherHornedStatueStructure NETHER_HORNED_STATUE = new NetherHornedStatueStructure();
	public static final IStructurePieceType NETHER_HORNED_STATUE_PIECE_TYPE = NetherHornedStatuePiece::new;
	public static final StructureFeature<?, ?> NETHER_HORNED_STATUE_CONFIGURED = NETHER_HORNED_STATUE.withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG);
	public static final StructureSeparationSettings NETHER_HORNED_STATUE_SEPARATION_SETTINGS = new StructureSeparationSettings(
			64, // spacing
			8, // separation
			1973135446
	);

	public static final TarreyTownGoddessStatueStructure TARREY_TOWN_GODDESS_STATUE = new TarreyTownGoddessStatueStructure();
	public static final IStructurePieceType TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE = TarreyTownGoddessStatuePiece::new;
	public static final StructureFeature<?, ?> TARREY_TOWN_GODDESS_STATUE_CONFIGURED = TARREY_TOWN_GODDESS_STATUE.withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG);
	public static final StructureSeparationSettings TARREY_TOWN_GODDESS_STATUE_SEPARATION_SETTINGS = new StructureSeparationSettings(
			64, // spacing
			8, // separation
			850796625
	);

	@SubscribeEvent
	public static void registerStructure(RegistryEvent.Register<Structure<?>> event){
		IForgeRegistry<Structure<?>> registry = event.getRegistry();
		register(registry,
				UNDERGROUND_HORNED_STATUE,
				UNDERGROUND_HORNED_STATUE_PIECE_TYPE,
				UNDERGROUND_HORNED_STATUE_CONFIGURED,
				UNDERGROUND_HORNED_STATUE_SEPARATION_SETTINGS,
				"underground_horned_statue");
		register(registry,
				NETHER_HORNED_STATUE,
				NETHER_HORNED_STATUE_PIECE_TYPE,
				NETHER_HORNED_STATUE_CONFIGURED,
				NETHER_HORNED_STATUE_SEPARATION_SETTINGS,
				"nether_horned_statue");
		register(registry,
				TARREY_TOWN_GODDESS_STATUE,
				TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE,
				TARREY_TOWN_GODDESS_STATUE_CONFIGURED,
				TARREY_TOWN_GODDESS_STATUE_SEPARATION_SETTINGS,
				"tarrey_town_goddess_statue");
	}

	private static void register(IForgeRegistry<Structure<?>> registry,
	                             Structure<?> structure,
	                             IStructurePieceType structurePieceType,
	                             StructureFeature<?, ?> structureFeature,
	                             StructureSeparationSettings separationSettings,
	                             String structureName){
		ResourceLocation key = new ResourceLocation(MODID, structureName);
		registry.register(structure.setRegistryName(key));

		Structure.NAME_STRUCTURE_BIMAP.put(key.toString(), structure);
		DimensionStructuresSettings.field_236191_b_ =
				ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
						.putAll(DimensionStructuresSettings.field_236191_b_)
						.put(structure, separationSettings)
						.build();

		Registry.register(Registry.STRUCTURE_PIECE, key, structurePieceType);

		Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, key, structureFeature);
		FlatGenerationSettings.STRUCTURES.put(structure, structureFeature);
	}
}
