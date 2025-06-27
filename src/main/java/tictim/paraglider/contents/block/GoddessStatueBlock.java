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
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.contents.ParagliderAdvancements;

public class GoddessStatueBlock extends BaseStatueBlock {
	public static final MapCodec<GoddessStatueBlock> CODEC = simpleCodec(GoddessStatueBlock::new);

	private static final VoxelShape SHAPE_NORTH = Shapes.or(box(4, 16, 5, 12, 20, 11),
			box(4, 16, 11, 12, 19, 12),
			box(4, 12, 5, 12, 16, 12),
			box(5, 16, 4, 11, 19, 5),
			box(5, 12, 4, 11, 16, 5),
			box(3, 0, 3, 13, 12, 13)).optimize();
	private static final VoxelShape SHAPE_EAST = Shapes.or(box(5, 16, 4, 11, 20, 12),
			box(4, 16, 4, 5, 19, 12),
			box(4, 12, 4, 11, 16, 12),
			box(11, 16, 5, 12, 19, 11),
			box(11, 12, 5, 12, 16, 11),
			box(3, 0, 3, 13, 12, 13)).optimize();
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(box(4, 16, 5, 12, 20, 11),
			box(4, 16, 4, 12, 19, 5),
			box(4, 12, 4, 12, 16, 11),
			box(5, 16, 11, 11, 19, 12),
			box(5, 12, 11, 11, 16, 12),
			box(3, 0, 3, 13, 12, 13)).optimize();
	private static final VoxelShape SHAPE_WEST = Shapes.or(box(5, 16, 4, 11, 20, 12),
			box(11, 16, 4, 12, 19, 12),
			box(5, 12, 4, 12, 16, 12),
			box(4, 16, 5, 5, 19, 11),
			box(4, 12, 5, 5, 16, 11),
			box(3, 0, 3, 13, 12, 13)).optimize();

	public GoddessStatueBlock(Properties properties) {
		super(properties);
	}

	@Override protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override public @NotNull VoxelShape getShape(BlockState state,
	                                              @NotNull BlockGetter level,
	                                              @NotNull BlockPos pos,
	                                              @NotNull CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case EAST -> SHAPE_EAST;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			default -> SHAPE_NORTH;
		};
	}

	@Override protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
	                                                              Level level,
	                                                              @NotNull BlockPos pos,
	                                                              @NotNull Player player,
	                                                              @NotNull BlockHitResult hit) {
		if (!level.isClientSide) {
			BargainHandler.initiate(
					player,
					ParagliderBargainTypes.GODDESS_STATUE,
					pos,
					ParagliderAdvancements.PRAY_TO_THE_GODDESS,
					new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5));
		}
		return InteractionResult.SUCCESS;
	}
}
