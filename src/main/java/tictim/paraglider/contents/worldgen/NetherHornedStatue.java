package tictim.paraglider.contents.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import tictim.paraglider.contents.Contents;

import java.util.Optional;

import static tictim.paraglider.ParagliderMod.MODID;

public class NetherHornedStatue extends Structure{
	public static final Codec<NetherHornedStatue> CODEC = RecordCodecBuilder.create((inst) ->
			inst.group(settingsCodec(inst), HeightProvider.CODEC.fieldOf("height").forGetter(s -> s.height))
					.apply(inst, NetherHornedStatue::new));
	private static final ResourceLocation TEMPLATE = new ResourceLocation(MODID, "nether_horned_statue");

	public final HeightProvider height;

	public NetherHornedStatue(StructureSettings structureSettings, HeightProvider height){
		super(structureSettings);
		this.height = height;
	}

	/**
	 * @see net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure
	 */
	@Override public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext ctx){
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
		return Optional.of(new Structure.GenerationStub(pos, b ->
				b.addPiece(new Piece(ctx.structureTemplateManager(), Rotation.getRandom(r), pos))));
	}

	@Override public StructureType<?> type(){
		return Contents.NETHER_HORNED_STATUE.get();
	}

	public static StructurePieceType.StructureTemplateType pieceType(){
		return Piece::new;
	}

	@Override public GenerationStep.Decoration step(){
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static class Piece extends BaseHornedStatuePiece{
		private static final BlockPos PIVOT = new BlockPos(2, 1, 2);

		public Piece(StructureTemplateManager structureManager, Rotation rotation, BlockPos templatePos){
			super(Contents.PieceTypes.NETHER_HORNED_STATUE.get(), structureManager, TEMPLATE, rotation, templatePos);
			this.placeSettings.setRotationPivot(PIVOT);
		}
		public Piece(StructureTemplateManager structureManager, CompoundTag tag){
			super(Contents.PieceTypes.NETHER_HORNED_STATUE.get(), structureManager, tag);
			this.placeSettings.setRotationPivot(PIVOT);
		}
	}
}
