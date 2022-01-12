package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import tictim.paraglider.contents.ModStructures;

import java.util.Random;

import static net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier.simple;
import static tictim.paraglider.ParagliderMod.MODID;

public class UndergroundHornedStatue extends StructureFeature<NoneFeatureConfiguration>{
	public UndergroundHornedStatue(){
		super(NoneFeatureConfiguration.CODEC, simple(c -> true, UndergroundHornedStatue::generatePieces));
	}

	private static void generatePieces(StructurePiecesBuilder builder, PieceGenerator.Context<NoneFeatureConfiguration> context){
		BlockPos pos = new BlockPos(context.chunkPos().getMinBlockX(), 90, context.chunkPos().getMinBlockZ());
		Rotation rotation = Rotation.getRandom(context.random());
		addPieces(context.structureManager(), pos, rotation, builder);
	}

	public static void addPieces(StructureManager structureManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieces){
		pieces.addPiece(new Piece(structureManager, rotation, pos));
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
	}

	public static class Piece extends BaseHornedStatuePiece{
		private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "underground_horned_statue");

		public Piece(StructureManager structureManager, Rotation rotation, BlockPos templatePos){
			super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, structureManager, TEMPLATE, rotation, templatePos);
		}
		public Piece(StructureManager structureManager, CompoundTag tag){
			super(ModStructures.UNDERGROUND_HORNED_STATUE_PIECE_TYPE, structureManager, tag);
		}

		@Override public void postProcess(WorldGenLevel level,
		                                  StructureFeatureManager structureFeatureManager,
		                                  ChunkGenerator chunkGenerator,
		                                  Random random,
		                                  BoundingBox box,
		                                  ChunkPos chunkPos,
		                                  BlockPos pos){
			BlockPos pos2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(placeSettings, BlockPos.ZERO));

			int height = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos2.getX(), pos2.getZ());
			this.templatePosition = new BlockPos(templatePosition.getX(), height-(random.nextInt(30)+15), templatePosition.getZ());
			super.postProcess(level, structureFeatureManager, chunkGenerator, random, box, chunkPos, pos);
		}

		@Override public NoiseEffect getNoiseEffect(){
			return NoiseEffect.NONE;
		}
	}
}
