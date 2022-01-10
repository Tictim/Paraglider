package tictim.paraglider.contents;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;

import java.util.HashMap;
import java.util.Map;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ModStructures{
	private ModStructures(){}

	public static final UndergroundHornedStatue UNDERGROUND_HORNED_STATUE = new UndergroundHornedStatue();
	public static final StructurePieceType UNDERGROUND_HORNED_STATUE_PIECE_TYPE = UndergroundHornedStatue.pieceType();
	public static final ConfiguredStructureFeature<?, ?> UNDERGROUND_HORNED_STATUE_CONFIGURED = UNDERGROUND_HORNED_STATUE.configured(NoneFeatureConfiguration.NONE);
	public static final StructureFeatureConfiguration UNDERGROUND_HORNED_STATUE_SEPARATION_SETTINGS = new StructureFeatureConfiguration(
			16, // spacing
			8, // separation
			49788929 // chosen by fair nextInt(Integer.MAX_VALUE) roll.
			// guaranteed to be random.
	);

	public static final NetherHornedStatue NETHER_HORNED_STATUE = new NetherHornedStatue();
	public static final StructurePieceType NETHER_HORNED_STATUE_PIECE_TYPE = NetherHornedStatue.pieceType();
	public static final ConfiguredStructureFeature<?, ?> NETHER_HORNED_STATUE_CONFIGURED = NETHER_HORNED_STATUE.configured(NoneFeatureConfiguration.NONE);
	public static final StructureFeatureConfiguration NETHER_HORNED_STATUE_SEPARATION_SETTINGS = new StructureFeatureConfiguration(
			32, // spacing
			8, // separation
			1973135446
	);

	public static final TarreyTownGoddessStatue TARREY_TOWN_GODDESS_STATUE = new TarreyTownGoddessStatue();
	public static final StructurePieceType TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE = TarreyTownGoddessStatue.pieceType();
	public static final ConfiguredStructureFeature<?, ?> TARREY_TOWN_GODDESS_STATUE_CONFIGURED = TARREY_TOWN_GODDESS_STATUE.configured(NoneFeatureConfiguration.NONE);
	public static final StructureFeatureConfiguration TARREY_TOWN_GODDESS_STATUE_SEPARATION_SETTINGS = new StructureFeatureConfiguration(
			32, // spacing
			8, // separation
			850796625
	);

	@SubscribeEvent
	public static void registerStructure(RegistryEvent.Register<StructureFeature<?>> event){
		IForgeRegistry<StructureFeature<?>> registry = event.getRegistry();
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

	private static void register(IForgeRegistry<StructureFeature<?>> registry,
	                             StructureFeature<?> structure,
	                             StructurePieceType structurePieceType,
	                             ConfiguredStructureFeature<?, ?> configured,
	                             StructureFeatureConfiguration config,
	                             String structureName){
		ResourceLocation key = new ResourceLocation(MODID, structureName);
		registry.register(structure.setRegistryName(key));

		StructureFeature.STRUCTURES_REGISTRY.put(key.toString(), structure);
		StructureSettings.DEFAULTS =
				ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
						.putAll(StructureSettings.DEFAULTS)
						.put(structure, config)
						.build();

		Registry.register(Registry.STRUCTURE_PIECE, key, structurePieceType);

		Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, key, configured);

		BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
			Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.getValue().structureSettings().structureConfig();

			if(structureMap instanceof ImmutableMap){
				Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
				tempMap.put(structure, config);
				settings.getValue().structureSettings().structureConfig = tempMap;
			}
			else{
				structureMap.put(structure, config);
			}
		});
	}
}
