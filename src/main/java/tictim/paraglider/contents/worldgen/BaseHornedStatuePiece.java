package tictim.paraglider.contents.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
public class BaseHornedStatuePiece extends TemplateStructurePiece {
	public BaseHornedStatuePiece(StructurePieceType type,
	                             StructureTemplateManager structureManager,
	                             Identifier location,
	                             BlockPos templatePos) {
		super(type,
				0,
				structureManager,
				location,
				location.toString(),
				new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK),
				templatePos);
	}
	public BaseHornedStatuePiece(StructurePieceType type,
	                             CompoundTag tag,
	                             StructureTemplateManager structureManager,
	                             Function<Identifier, StructurePlaceSettings> placeSettingsFactory,
	                             boolean savePivot) {
		super(type, tag, structureManager, placeSettingsFactory);
		this.savePivot = savePivot;
	}

	public static StructureTemplateType createType(Supplier<StructurePieceType> type) {
		return createType(type, null);
	}

	public static StructureTemplateType createType(Supplier<StructurePieceType> type, @Nullable BlockPos pivot) {
		return (templateManager, tag) -> {
			var oRot = tag.getString("Rot");
			var oRotPivot = tag.read("RotPivot", BlockPos.CODEC);

			return new BaseHornedStatuePiece(
					type.get(),
					tag,
					templateManager,
					l -> {
						StructurePlaceSettings s = new StructurePlaceSettings()
								.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
						if (oRot.isPresent()) {
							s.setRotation(Rotation.valueOf(oRot.get()));
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

	public BaseHornedStatuePiece rot(BlockPos pivot, Rotation rot) {
		return rot(pivot, rot, true);
	}

	private boolean savePivot;

	public BaseHornedStatuePiece rot(BlockPos pivot, Rotation rot, boolean savePivot) {
		this.placeSettings.setRotationPivot(pivot).setRotation(rot);
		this.savePivot = savePivot;
		return this;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		if (getRotation() != Rotation.NONE) {
			tag.putString("Rot", getRotation().name());
			if (this.savePivot)
				tag.store("RotPivot", BlockPos.CODEC, this.placeSettings.getRotationPivot());
		}
	}

	@Override
	protected void handleDataMarker(String function,
	                                BlockPos pos,
	                                ServerLevelAccessor accessor,
	                                RandomSource random,
	                                BoundingBox boundingBox) {}
}
