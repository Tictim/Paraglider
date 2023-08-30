package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class BaseHornedStatuePiece extends TemplateStructurePiece{
	public BaseHornedStatuePiece(@NotNull StructurePieceType type,
	                             @NotNull StructureTemplateManager structureManager,
	                             @NotNull ResourceLocation location,
	                             @NotNull BlockPos templatePos){
		super(type,
				0,
				structureManager,
				location,
				location.toString(),
				new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK),
				templatePos);
	}
	public BaseHornedStatuePiece(@NotNull StructurePieceType type,
	                             @NotNull CompoundTag tag,
	                             @NotNull StructureTemplateManager structureManager,
	                             @NotNull Function<ResourceLocation, StructurePlaceSettings> placeSettingsFactory,
	                             boolean savePivot){
		super(type, tag, structureManager, placeSettingsFactory);
		this.savePivot = savePivot;
	}

	@NotNull public static StructurePieceType.StructureTemplateType createType(@NotNull Supplier<StructurePieceType> type){
		return createType(type, null);
	}
	@NotNull public static StructurePieceType.StructureTemplateType createType(@NotNull Supplier<StructurePieceType> type, @Nullable BlockPos pivot){
		return (templateManager, tag) -> new BaseHornedStatuePiece(type.get(), tag, templateManager, l -> {
			StructurePlaceSettings s = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
			if(tag.contains("Rot", Tag.TAG_STRING)){
				s.setRotation(Rotation.valueOf(tag.getString("Rot")));
				if(pivot!=null){
					s.setRotationPivot(pivot);
				}else if(tag.contains("RotPivot", Tag.TAG_COMPOUND)){
					s.setRotationPivot(NbtUtils.readBlockPos(tag.getCompound("RotPivot")));
				}
			}
			return s;
		}, pivot!=null||tag.contains("RotPivot", Tag.TAG_COMPOUND));
	}

	@NotNull public BaseHornedStatuePiece rot(@NotNull BlockPos pivot, @NotNull Rotation rot){
		return rot(pivot, rot, true);
	}

	private boolean savePivot;

	@NotNull public BaseHornedStatuePiece rot(@NotNull BlockPos pivot, @NotNull Rotation rot, boolean savePivot){
		this.placeSettings.setRotationPivot(pivot).setRotation(rot);
		this.savePivot = savePivot;
		return this;
	}

	@Override protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag){
		super.addAdditionalSaveData(context, tag);
		if(getRotation()!=Rotation.NONE){
			tag.putString("Rot", getRotation().name());
			if(savePivot)
				tag.put("RotPivot", NbtUtils.writeBlockPos(placeSettings.getRotationPivot()));
		}
	}

	@Override protected void handleDataMarker(@NotNull String function,
	                                          @NotNull BlockPos pos,
	                                          @NotNull ServerLevelAccessor accessor,
	                                          @NotNull RandomSource random,
	                                          @NotNull BoundingBox boundingBox){}
}
