package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.StructureTemplateType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class BaseHornedStatuePiece extends TemplateStructurePiece {
	public BaseHornedStatuePiece(@NotNull StructurePieceType type,
	                             @NotNull StructureTemplateManager structureManager,
	                             @NotNull ResourceLocation location,
	                             @NotNull BlockPos templatePos) {
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
	                             boolean savePivot) {
		super(type, tag, structureManager, placeSettingsFactory);
		this.savePivot = savePivot;
	}

	public static @NotNull StructureTemplateType createType(@NotNull Supplier<StructurePieceType> type) {
		return createType(type, null);
	}

	public static @NotNull StructureTemplateType createType(@NotNull Supplier<StructurePieceType> type, @Nullable BlockPos pivot) {
		return (templateManager, tag) -> {
			var oRot = tag.getString("Rot");
			var oRotPivot = NbtUtils.readBlockPos(tag, "RotPivot");

			return new BaseHornedStatuePiece(
					type.get(),
					tag,
					templateManager,
					l -> {
						StructurePlaceSettings s = new StructurePlaceSettings()
								.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
						if (!oRot.isEmpty()) {
							s.setRotation(Rotation.valueOf(oRot));
							if (pivot != null) {
								s.setRotationPivot(pivot);
							} else {
								oRotPivot.ifPresent(s::setRotationPivot);
							}
						}
						return s;
					},
					pivot != null || oRotPivot.isPresent());
		};
	}

	public @NotNull BaseHornedStatuePiece rot(@NotNull BlockPos pivot, @NotNull Rotation rot) {
		return rot(pivot, rot, true);
	}

	private boolean savePivot;

	public @NotNull BaseHornedStatuePiece rot(@NotNull BlockPos pivot, @NotNull Rotation rot, boolean savePivot) {
		this.placeSettings.setRotationPivot(pivot).setRotation(rot);
		this.savePivot = savePivot;
		return this;
	}

	@Override
	protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		if (getRotation() != Rotation.NONE) {
			tag.putString("Rot", getRotation().name());
			if (this.savePivot)
				tag.put("RotPivot", NbtUtils.writeBlockPos(this.placeSettings.getRotationPivot()));
		}
	}

	@Override
	protected void handleDataMarker(@NotNull String function,
	                                @NotNull BlockPos pos,
	                                @NotNull ServerLevelAccessor accessor,
	                                @NotNull RandomSource random,
	                                @NotNull BoundingBox boundingBox) {}
}
