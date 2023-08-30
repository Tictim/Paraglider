package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

public class UndergroundHornedStatue extends Structure{
	public static final Codec<UndergroundHornedStatue> CODEC = simpleCodec(UndergroundHornedStatue::new);
	private static final ResourceLocation TEMPLATE = ParagliderAPI.id("underground_horned_statue");

	@NotNull public static StructurePieceType.StructureTemplateType pieceType(){
		return BaseHornedStatuePiece.createType(() -> Contents.get().undergroundHornedStatuePiece(), BlockPos.ZERO);
	}

	public UndergroundHornedStatue(@NotNull StructureSettings structureSettings){
		super(structureSettings);
	}

	@Override @NotNull public Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext ctx){
		int x = ctx.chunkPos().getMinBlockX()+ctx.random().nextInt(16);
		int z = ctx.chunkPos().getMinBlockZ()+ctx.random().nextInt(16);
		int y = searchY(ctx, x, z);
		if(y==Integer.MIN_VALUE) return Optional.empty();

		BlockPos pos = new BlockPos(x, y, z);
		Rotation rotation = Rotation.getRandom(ctx.random());
		return Optional.of(new GenerationStub(pos, b ->
				b.addPiece(new BaseHornedStatuePiece(Contents.get().undergroundHornedStatuePiece(), ctx.structureTemplateManager(), TEMPLATE, pos)
						.rot(BlockPos.ZERO, rotation))));
	}

	private static int searchY(@NotNull GenerationContext ctx, int x, int z){
		int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState())
				-ctx.random().nextInt(30);
		int depth = 15+2;
		NoiseColumn col = ctx.chunkGenerator().getBaseColumn(x, z, ctx.heightAccessor(), ctx.randomState());
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, y, z);
		for(; y>-32; y--){
			BlockState state = col.getBlock(y);
			if(state.getCollisionShape(EmptyBlockGetter.INSTANCE, mpos.setY(y)).isEmpty()){
				if(depth<5) depth = 5;
			}else if(--depth<=0) return y;
		}
		return Integer.MIN_VALUE; // cannot generate structure
	}

	@Override @NotNull public StructureType<?> type(){
		return Contents.get().undergroundHornedStatue();
	}
	@Override @NotNull public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.UNDERGROUND_STRUCTURES;
	}
}
