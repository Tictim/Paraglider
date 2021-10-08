package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Random;

public abstract class BaseHornedStatuePiece extends TemplateStructurePiece{
	public BaseHornedStatuePiece(StructurePieceType type, StructureManager structureManager, ResourceLocation location, Rotation rotation, BlockPos templatePos){
		super(type, 0, structureManager, location, location.toString(), makeSettings(rotation), templatePos);
	}
	public BaseHornedStatuePiece(StructurePieceType type, ServerLevel level, CompoundTag tag){
		super(type, tag, level, l -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
	}

	private static StructurePlaceSettings makeSettings(Rotation pRotation){
		return new StructurePlaceSettings().setRotation(pRotation);
		// .setRotationPivot(IglooPieces.PIVOTS.get(pLocation)) TODO hmm
	}

	@Override protected void addAdditionalSaveData(ServerLevel level, CompoundTag tag){
		super.addAdditionalSaveData(level, tag);
		tag.putString("Rot", getRotation().name());
	}

	@Override protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand, BoundingBox sbb){}
}
