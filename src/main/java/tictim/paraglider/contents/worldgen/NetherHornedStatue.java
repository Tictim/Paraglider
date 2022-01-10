package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import tictim.paraglider.contents.ModStructures;

import java.util.Random;

import static net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.checkForBiomeOnTop;
import static net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.simple;
import static tictim.paraglider.ParagliderMod.MODID;

public class NetherHornedStatue extends StructureFeature<NoneFeatureConfiguration>{
	public NetherHornedStatue(){
		super(NoneFeatureConfiguration.CODEC, simple(checkForBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG), NetherHornedStatue::generatePieces));
	}

	private static void generatePieces(StructurePiecesBuilder builder, PieceGenerator.Context<NoneFeatureConfiguration> context){
		BlockPos pos = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
		Rotation rotation = Rotation.getRandom(context.random());
		addPieces(context.structureManager(), pos, rotation, builder, context.random());
	}

	public static void addPieces(StructureManager structureManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieces, Random random){
		pieces.addPiece(new Piece(structureManager, rotation, pos));
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static class Piece extends BaseHornedStatuePiece{
		private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "nether_horned_statue");

		public Piece(StructureManager structureManager, Rotation rotation, BlockPos templatePos){
			super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, structureManager, TEMPLATE, rotation, templatePos);
		}
		public Piece(StructureManager structureManager, CompoundTag tag){
			super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, structureManager, tag);
		}
	}
}
