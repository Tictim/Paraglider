package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

import static tictim.paraglider.ParagliderMod.MODID;

public class UndergroundHornedStatue extends Structure{
	public static final Codec<UndergroundHornedStatue> CODEC = simpleCodec(UndergroundHornedStatue::new);
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "underground_horned_statue");

	public UndergroundHornedStatue(StructureSettings structureSettings){
		super(structureSettings);
	}

	@Override public Optional<GenerationStub> findGenerationPoint(GenerationContext p_226571_){
		// TODO shit
		return Optional.empty();
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
	}

	@Override public StructureType<?> type(){
		return Contents.UNDERGROUND_HORNED_STATUE.get();
	}

	public static class Piece extends BaseHornedStatuePiece{
		public Piece(StructureTemplateManager structureManager, Rotation rotation, BlockPos templatePos){
			super(Contents.PieceTypes.UNDERGROUND_HORNED_STATUE.get(), structureManager, TEMPLATE, rotation, templatePos);
		}
		public Piece(StructureTemplateManager structureManager, CompoundTag tag){
			super(Contents.PieceTypes.UNDERGROUND_HORNED_STATUE.get(), structureManager, tag);
		}
	}
}
