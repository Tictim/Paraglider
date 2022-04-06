package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import tictim.paraglider.contents.ModStructures;

import java.util.Optional;
import java.util.Random;

import static tictim.paraglider.ParagliderMod.MODID;

public class NetherHornedStatue extends StructureFeature<RangeConfiguration>{
	public NetherHornedStatue(){
		super(RangeConfiguration.CODEC, NetherHornedStatue::createGenerator);
	}

	/**
	 * @see net.minecraft.world.level.levelgen.structure.NetherFossilFeature
	 */
	private static Optional<PieceGenerator<RangeConfiguration>> createGenerator(PieceGeneratorSupplier.Context<RangeConfiguration> c){
		WorldgenRandom r = new WorldgenRandom(new LegacyRandomSource(0L));
		ChunkPos chunkPos = c.chunkPos();
		r.setLargeFeatureSeed(c.seed(), chunkPos.x, chunkPos.z);
		int x = chunkPos.getMinBlockX()+r.nextInt(16);
		int y = chunkPos.getMinBlockZ()+r.nextInt(16);
		int seaLevel = c.chunkGenerator().getSeaLevel();
		WorldGenerationContext wgc = new WorldGenerationContext(c.chunkGenerator(), c.heightAccessor());
		int wat = c.config().height.sample(r, wgc);
		NoiseColumn noise = c.chunkGenerator().getBaseColumn(x, y, c.heightAccessor());
		MutableBlockPos mpos = new MutableBlockPos(x, wat, y);

		while(wat>seaLevel){
			BlockState s = noise.getBlock(wat);
			--wat;
			BlockState s2 = noise.getBlock(wat);
			if(s.isAir()&&(s2.is(Blocks.SOUL_SAND)||s2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mpos.setY(wat), Direction.UP))) break;
		}

		if(wat<=seaLevel||!c.validBiome().test(c.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(wat), QuartPos.fromBlock(y)))) return Optional.empty();
		BlockPos pos2 = new BlockPos(x, wat, y);
		return Optional.of((builder, context) -> builder.addPiece(new Piece(c.structureManager(), Rotation.getRandom(r), pos2)));
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static class Piece extends BaseHornedStatuePiece{
		private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "nether_horned_statue");
		private static final BlockPos PIVOT = new BlockPos(2, 1, 2);

		public Piece(StructureManager structureManager, Rotation rotation, BlockPos templatePos){
			super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, structureManager, TEMPLATE, rotation, templatePos);
			this.placeSettings.setRotationPivot(PIVOT);
		}
		public Piece(StructureManager structureManager, CompoundTag tag){
			super(ModStructures.NETHER_HORNED_STATUE_PIECE_TYPE, structureManager, tag);
			this.placeSettings.setRotationPivot(PIVOT);
		}

		@Override public void postProcess(WorldGenLevel level,
		                                  StructureFeatureManager structureFeatureManager,
		                                  ChunkGenerator chunkGenerator,
		                                  Random random,
		                                  BoundingBox box,
		                                  ChunkPos chunkPos,
		                                  BlockPos pos){
			BlockPos pos2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(placeSettings, PIVOT));

			int seaLevel = chunkGenerator.getSeaLevel();
			int y = seaLevel+random.nextInt(chunkGenerator.getGenDepth()-2-seaLevel);
			NoiseColumn baseColumn = chunkGenerator.getBaseColumn(pos2.getX(), pos2.getZ(), level);

			for(MutableBlockPos mpos = new MutableBlockPos(pos2.getX(), y, pos2.getZ()); y>seaLevel; --y){
				BlockState state = baseColumn.getBlock(y);
				BlockState downState = baseColumn.getBlock(y-1);
				if(state.isAir()&&(downState.is(Blocks.SOUL_SAND)||downState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mpos.setY(y-1), Direction.UP))) break;
			}

			if(y>seaLevel){
				this.templatePosition = new BlockPos(templatePosition.getX(), y, templatePosition.getZ());
				super.postProcess(level, structureFeatureManager, chunkGenerator, random, box, chunkPos, pos);
			}
		}
	}
}
