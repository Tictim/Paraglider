package tictim.paraglider.contents;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ModStructures{
	private ModStructures(){}

	public static final StructurePieceType UNDERGROUND_HORNED_STATUE_PIECE_TYPE = UndergroundHornedStatue.pieceType();
	public static final StructurePieceType NETHER_HORNED_STATUE_PIECE_TYPE = NetherHornedStatue.pieceType();
	public static final StructurePieceType TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE = TarreyTownGoddessStatue.pieceType();

	@SubscribeEvent
	public static void registerStructure(RegistryEvent.Register<StructureFeature<?>> event){
		Registry.register(Registry.STRUCTURE_PIECE, Contents.UNDERGROUND_HORNED_STATUE.getId(), UNDERGROUND_HORNED_STATUE_PIECE_TYPE);
		Registry.register(Registry.STRUCTURE_PIECE, Contents.NETHER_HORNED_STATUE.getId(), NETHER_HORNED_STATUE_PIECE_TYPE);
		Registry.register(Registry.STRUCTURE_PIECE, Contents.TARREY_TOWN_GODDESS_STATUE.getId(), TARREY_TOWN_GODDESS_STATUE_PIECE_TYPE);
	}
}
