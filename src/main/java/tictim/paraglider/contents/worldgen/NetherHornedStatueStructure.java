package tictim.paraglider.contents.worldgen;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherHornedStatueStructure extends StructureFeature<NoneFeatureConfiguration>{
	public NetherHornedStatueStructure(){
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override public StructureStartFactory<NoneFeatureConfiguration> getStartFactory(){
		return Start::new;
	}
	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static final class Start extends StructureStart<NoneFeatureConfiguration>{
		public Start(StructureFeature<NoneFeatureConfiguration> pFeature, ChunkPos pChunkPos, int pReferences, long pSeed){
			super(pFeature, pChunkPos, pReferences, pSeed);
		}

		@Override public void generatePieces(RegistryAccess registry,
		                                     ChunkGenerator chunkGenerator,
		                                     StructureManager structureManager,
		                                     ChunkPos chunkPos,
		                                     Biome biome,
		                                     NoneFeatureConfiguration config,
		                                     LevelHeightAccessor level){
			Rotation rotation = Util.getRandom(Rotation.values(), this.random);

			int x = chunkPos.getMinBlockX()+this.random.nextInt(16), z = chunkPos.getMinBlockZ()+this.random.nextInt(16);

			int seaLevel = chunkGenerator.getSeaLevel();
			int y = seaLevel+this.random.nextInt(chunkGenerator.getGenDepth()-2-seaLevel);
			NoiseColumn baseColumn = chunkGenerator.getBaseColumn(x, z, level);

			for(BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, y, z); y>seaLevel; --y){
				BlockState state = baseColumn.getBlockState(mpos);
				mpos.move(Direction.DOWN);
				BlockState downState = baseColumn.getBlockState(mpos);
				if(state.isAir()&&(downState.is(Blocks.SOUL_SAND)||downState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mpos, Direction.UP))) break;
			}

			if(y>seaLevel)
				addPiece(new NetherHornedStatuePiece(structureManager, rotation, new BlockPos(-2, -1, -2).rotate(rotation).offset(x, y, z)));
		}
	}
}
