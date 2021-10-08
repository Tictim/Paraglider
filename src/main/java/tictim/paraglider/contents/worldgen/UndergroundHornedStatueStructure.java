package tictim.paraglider.contents.worldgen;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class UndergroundHornedStatueStructure extends StructureFeature<NoneFeatureConfiguration>{
	public UndergroundHornedStatueStructure(){
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override public StructureStartFactory<NoneFeatureConfiguration> getStartFactory(){
		return Start::new;
	}
	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
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

			int surfaceY = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level);
			if(surfaceY>=40) addPiece(new UndergroundHornedStatuePiece(structureManager, rotation, new BlockPos(x, this.random.nextInt(surfaceY-30)+25, z)));
		}
	}
}
