package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

import static tictim.paraglider.ParagliderMod.MODID;

public class TarreyTownGoddessStatue extends Structure{
	public static final Codec<TarreyTownGoddessStatue> CODEC = simpleCodec(TarreyTownGoddessStatue::new);
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "tarrey_town_goddess_statue");

	public static StructurePieceType.StructureTemplateType pieceType(){
		return BaseHornedStatuePiece.createType(Contents.TARREY_TOWN_GODDESS_STATUE_PIECE);
	}

	public TarreyTownGoddessStatue(StructureSettings structureSettings){
		super(structureSettings);
	}

	@Override public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx){
		StructureTemplate t = ctx.structureTemplateManager().getOrCreate(TEMPLATE);

		Rotation rotation = Rotation.getRandom(ctx.random());
		BlockPos worldPos = ctx.chunkPos().getWorldPosition();
		BlockPos pivot = new BlockPos(t.getSize().getX()/2, 0, t.getSize().getZ()/2);
		BlockPos center = t.getBoundingBox(worldPos, rotation, pivot, Mirror.NONE).getCenter();
		int y = ctx.chunkGenerator().getFirstOccupiedHeight(center.getX(), center.getZ(),
				Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState())-1;
		BlockPos pos = new BlockPos(worldPos.getX(), y, worldPos.getZ());
		return Optional.of(new GenerationStub(pos, b ->
				b.addPiece(new BaseHornedStatuePiece(Contents.TARREY_TOWN_GODDESS_STATUE_PIECE.get(), ctx.structureTemplateManager(), TEMPLATE, pos)
						.rot(pivot, rotation))));
	}

	@Override public StructureType<?> type(){
		return Contents.TARREY_TOWN_GODDESS_STATUE.get();
	}
	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.LAKES;
	}
}
