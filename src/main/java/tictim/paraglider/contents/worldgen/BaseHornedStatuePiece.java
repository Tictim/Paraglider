package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Random;

public abstract class BaseHornedStatuePiece extends TemplateStructurePiece{
	public BaseHornedStatuePiece(StructurePieceType type, StructureManager structureManager, ResourceLocation location, Rotation rotation, BlockPos templatePos){
		super(type, 0, structureManager, location, location.toString(), makeSettings(rotation), templatePos);
	}
	public BaseHornedStatuePiece(StructurePieceType type, StructureManager structureManager, CompoundTag tag){
		super(type, tag, structureManager, l -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
	}

	private static StructurePlaceSettings makeSettings(Rotation rot){
		return new StructurePlaceSettings()
				.setRotation(rot)
				.setMirror(Mirror.NONE)
				.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
	}

	@Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag){
		super.addAdditionalSaveData(context, tag);
		tag.putString("Rot", getRotation().name());
	}

	@Override protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand, BoundingBox sbb){}
}
