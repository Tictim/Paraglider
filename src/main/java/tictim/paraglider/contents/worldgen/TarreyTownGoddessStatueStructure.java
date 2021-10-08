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

public class TarreyTownGoddessStatueStructure extends StructureFeature<NoneFeatureConfiguration>{
	public TarreyTownGoddessStatueStructure(){
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override public StructureStartFactory<NoneFeatureConfiguration> getStartFactory(){
		return Start::new;
	}
	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.LAKES;
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
			int surfaceY = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level);
			addPiece(new TarreyTownGoddessStatuePiece(structureManager, rotation, new BlockPos(x, surfaceY-3, z)));
		}
	}
}
