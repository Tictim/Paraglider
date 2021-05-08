package tictim.paraglider.contents.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class NetherHornedStatueStructure extends Structure<NoFeatureConfig>{
	public NetherHornedStatueStructure(){
		super(NoFeatureConfig.field_236558_a_);
	}

	@Override public IStartFactory<NoFeatureConfig> getStartFactory(){
		return Start::new;
	}
	@Override public GenerationStage.Decoration getDecorationStage(){
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}

	public static final class Start extends StructureStart<NoFeatureConfig>{
		public Start(Structure<NoFeatureConfig> structure,
		             int chunkX,
		             int chunkZ,
		             MutableBoundingBox bounds,
		             int references,
		             long seed){
			super(structure, chunkX, chunkZ, bounds, references, seed);
		}

		@Override
		public void func_230364_a_(DynamicRegistries dynamicRegistries,
		                           ChunkGenerator chunkGenerator,
		                           TemplateManager templateManager,
		                           int chunkX,
		                           int chunkZ,
		                           Biome biome,
		                           NoFeatureConfig config){
			Rotation rotation = Util.getRandomObject(Rotation.values(), this.rand);

			ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
			int x = chunkPos.getXStart()+this.rand.nextInt(16), z = chunkPos.getZStart()+this.rand.nextInt(16);

			int seaLevel = chunkGenerator.getSeaLevel();
			int y = seaLevel+this.rand.nextInt(chunkGenerator.getMaxBuildHeight()-2-seaLevel);
			IBlockReader reader = chunkGenerator.func_230348_a_(x, z);

			for(BlockPos.Mutable mpos = new BlockPos.Mutable(x, y, z); y>seaLevel; --y){
				BlockState state = reader.getBlockState(mpos);
				mpos.move(Direction.DOWN);
				BlockState downState = reader.getBlockState(mpos);
				//noinspection deprecation
				if(state.isAir(reader, mpos)&&(downState.isIn(Blocks.SOUL_SAND)||downState.isSolidSide(reader, mpos, Direction.UP))){
					break;
				}
			}

			if(y>seaLevel){
				BlockPos pos = new BlockPos(-2, -1, -2).rotate(rotation).add(x, y, z);

				components.add(new NetherHornedStatuePiece(templateManager, pos, rotation));

				recalculateStructureSize();
			}
		}
	}
}
