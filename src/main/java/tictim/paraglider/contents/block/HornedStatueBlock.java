package tictim.paraglider.contents.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.contents.ParagliderAdvancements;

@NullMarked
public class HornedStatueBlock extends BaseStatueBlock {
	public static final MapCodec<HornedStatueBlock> CODEC = simpleCodec(HornedStatueBlock::new);

	private static final VoxelShape SHAPE_NORTH = Shapes.or(box(3, 0, 3, 13, 14, 12),
			box(5, 0, 2, 11, 12, 3),
			box(3, 0, 12, 13, 13, 13)).optimize();
	private static final VoxelShape SHAPE_EAST = Shapes.or(box(4, 0, 3, 13, 14, 13),
			box(13, 0, 5, 14, 12, 11),
			box(3, 0, 3, 4, 13, 13)).optimize();
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(box(3, 0, 4, 13, 14, 13),
			box(5, 0, 13, 11, 12, 14),
			box(3, 0, 3, 13, 13, 4)).optimize();
	private static final VoxelShape SHAPE_WEST = Shapes.or(box(3, 0, 3, 12, 14, 13),
			box(2, 0, 5, 3, 12, 11),
			box(12, 0, 3, 13, 13, 13)).optimize();

	public HornedStatueBlock(Properties properties) {
		super(properties);
	}

	@Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override public VoxelShape getShape(BlockState state,
	                                     BlockGetter level,
	                                     BlockPos pos,
	                                     CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case EAST -> SHAPE_EAST;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			default -> SHAPE_NORTH;
		};
	}

	@Override protected InteractionResult useWithoutItem(BlockState state,
	                                                     Level level,
	                                                     BlockPos pos,
	                                                     Player player,
	                                                     BlockHitResult hit) {
		if (!level.isClientSide()) {
			BargainHandler.initiate(
					player,
					ParagliderBargainTypes.HORNED_STATUE,
					pos,
					ParagliderAdvancements.STATUES_BARGAIN,
					new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
		}
		return InteractionResult.SUCCESS;
	}
}
