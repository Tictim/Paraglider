package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

public class NetherHornedStatue extends Structure{
	public static final Codec<NetherHornedStatue> CODEC = RecordCodecBuilder.create(b -> b.group(
			settingsCodec(b),
			HeightProvider.CODEC.fieldOf("height").forGetter(s -> s.height)
	).apply(b, NetherHornedStatue::new));
	private static final ResourceLocation TEMPLATE = ParagliderAPI.id("nether_horned_statue");
	private static final BlockPos PIVOT = new BlockPos(2, 1, 2);

	@NotNull public static StructurePieceType.StructureTemplateType pieceType(){
		return BaseHornedStatuePiece.createType(() -> Contents.get().netherHornedStatuePiece(), PIVOT);
	}

	public final HeightProvider height;

	public NetherHornedStatue(@NotNull StructureSettings structureSettings, @NotNull HeightProvider height){
		super(structureSettings);
		this.height = height;
	}

	/**
	 * @see net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure
	 */
	@Override @NotNull public Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext ctx){
		WorldgenRandom r = ctx.random();
		int x = ctx.chunkPos().getMinBlockX()+r.nextInt(16);
		int z = ctx.chunkPos().getMinBlockZ()+r.nextInt(16);
		int seaLevel = ctx.chunkGenerator().getSeaLevel();
		int y = this.height.sample(r, new WorldGenerationContext(ctx.chunkGenerator(), ctx.heightAccessor()));
		NoiseColumn col = ctx.chunkGenerator().getBaseColumn(x, z, ctx.heightAccessor(), ctx.randomState());
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, y, z);

		while(y>seaLevel){
			BlockState state = col.getBlock(y);
			--y;
			BlockState bottomState = col.getBlock(y);
			if(state.isAir()&&(bottomState.is(Blocks.SOUL_SAND)||
					bottomState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mpos.setY(y), Direction.UP))){
				break;
			}
		}
		if(y<=seaLevel) return Optional.empty();

		BlockPos pos = new BlockPos(x, y-1, z);
		return Optional.of(new GenerationStub(pos, b ->
				b.addPiece(new BaseHornedStatuePiece(Contents.get().netherHornedStatuePiece(), ctx.structureTemplateManager(), TEMPLATE, pos)
						.rot(PIVOT, Rotation.getRandom(r), false))));
	}

	@Override @NotNull public StructureType<?> type(){
		return Contents.get().netherHornedStatue();
	}
	@Override @NotNull public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}
}
