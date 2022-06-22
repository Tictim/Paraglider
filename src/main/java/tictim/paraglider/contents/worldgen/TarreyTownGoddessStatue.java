package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

import static tictim.paraglider.ParagliderMod.MODID;

public class TarreyTownGoddessStatue extends Structure{
	public static final Codec<TarreyTownGoddessStatue> CODEC = simpleCodec(TarreyTownGoddessStatue::new);
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "tarrey_town_goddess_statue");

	public TarreyTownGoddessStatue(StructureSettings structureSettings){
		super(structureSettings);
	}

	@Override public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx){
		StructureTemplate t = ctx.structureTemplateManager().getOrCreate(TEMPLATE);

		Rotation rotation = Util.getRandom(Rotation.values(), ctx.random());
		BlockPos worldPos = ctx.chunkPos().getWorldPosition();
		BlockPos center = t.getBoundingBox(worldPos, rotation,
						new BlockPos(t.getSize().getX()/2, 0, t.getSize().getZ()/2),
						ctx.random().nextFloat()<.5f ? Mirror.NONE : Mirror.FRONT_BACK)
				.getCenter();
		int y = ctx.chunkGenerator().getFirstOccupiedHeight(center.getX(), center.getZ(),
				Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState())-1;
		BlockPos pos = new BlockPos(worldPos.getX(), y, worldPos.getZ());
		return Optional.of(new GenerationStub(pos, b -> addPieces(ctx.structureTemplateManager(), pos, rotation, b)));
	}

	@Override public StructureType<?> type(){
		return Contents.TARREY_TOWN_GODDESS_STATUE.get();
	}

	public static void addPieces(StructureTemplateManager structureManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieces){
		pieces.addPiece(new Piece(structureManager, rotation, pos));
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.LAKES;
	}

	public static class Piece extends BaseHornedStatuePiece{
		public Piece(StructureTemplateManager structureManager, Rotation rotation, BlockPos templatePos){
			super(Contents.PieceTypes.TARREY_TOWN_GODDESS_STATUE.get(), structureManager, TEMPLATE, rotation, templatePos);
		}
		public Piece(StructureTemplateManager structureManager, CompoundTag tag){
			super(Contents.PieceTypes.TARREY_TOWN_GODDESS_STATUE.get(), structureManager, tag);
		}
	}
}
