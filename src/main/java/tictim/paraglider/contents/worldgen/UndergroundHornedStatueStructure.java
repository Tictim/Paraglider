package tictim.paraglider.contents.worldgen;

import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class UndergroundHornedStatueStructure extends Structure<NoFeatureConfig>{

	public UndergroundHornedStatueStructure(){
		super(NoFeatureConfig.field_236558_a_);
	}

	@Override public IStartFactory<NoFeatureConfig> getStartFactory(){
		return Start::new;
	}
	@Override public GenerationStage.Decoration getDecorationStage(){
		return GenerationStage.Decoration.UNDERGROUND_STRUCTURES;
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

			int surfaceY = chunkGenerator.getHeight(x, z, Heightmap.Type.OCEAN_FLOOR_WG);
			if(surfaceY>=40){
				BlockPos pos = new BlockPos(x, this.rand.nextInt(surfaceY-30)+25, z);

				components.add(new UndergroundHornedStatuePiece(templateManager, pos, rotation));

				recalculateStructureSize();
			}
		}
	}
}
