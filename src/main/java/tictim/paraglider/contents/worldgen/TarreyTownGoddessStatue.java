package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

public class TarreyTownGoddessStatue extends Structure {
	public static final MapCodec<TarreyTownGoddessStatue> CODEC = simpleCodec(TarreyTownGoddessStatue::new);
	private static final Identifier TEMPLATE = ParagliderAPI.id("tarrey_town_goddess_statue");

	public static @NotNull StructurePieceType.StructureTemplateType pieceType() {
		return BaseHornedStatuePiece.createType(() -> Contents.get().tarreyTownGoddessStatuePiece());
	}

	public TarreyTownGoddessStatue(@NotNull StructureSettings structureSettings) {
		super(structureSettings);
	}

	@Override public @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext ctx) {
		StructureTemplate t = ctx.structureTemplateManager().getOrCreate(TEMPLATE);

		Rotation rotation = Rotation.getRandom(ctx.random());
		BlockPos worldPos = ctx.chunkPos().getWorldPosition();
		BlockPos pivot = new BlockPos(t.getSize().getX() / 2, 0, t.getSize().getZ() / 2);
		BlockPos center = t.getBoundingBox(worldPos, rotation, pivot, Mirror.NONE).getCenter();
		int y = ctx.chunkGenerator().getFirstOccupiedHeight(center.getX(), center.getZ(),
				Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState()) - 1;
		BlockPos pos = new BlockPos(worldPos.getX(), y, worldPos.getZ());
		return Optional.of(new GenerationStub(pos, b ->
				b.addPiece(new BaseHornedStatuePiece(Contents.get().tarreyTownGoddessStatuePiece(), ctx.structureTemplateManager(), TEMPLATE, pos)
						.rot(pivot, rotation))));
	}

	@Override public @NotNull StructureType<?> type() {
		return Contents.get().tarreyTownGoddessStatue();
	}
	@Override public @NotNull GenerationStep.Decoration step() {
		return GenerationStep.Decoration.LAKES;
	}
}
