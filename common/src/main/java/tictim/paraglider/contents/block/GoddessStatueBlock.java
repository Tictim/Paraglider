package tictim.paraglider.contents.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.bargain.ParagliderBargainTypes;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.contents.ParagliderAdvancements;

import java.util.List;

public class GoddessStatueBlock extends HorizontalDirectionalBlock{
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

	@Nullable private final Component tooltip;

	public GoddessStatueBlock(@NotNull Properties properties){
		this(properties, null);
	}
	public GoddessStatueBlock(@NotNull Properties properties, @Nullable Component tooltip){
		super(properties);
		this.tooltip = tooltip;
	}

	@Override protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder){
		builder.add(FACING);
	}

	@Override @Nullable public BlockState getStateForPlacement(@NotNull BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Override @NotNull public VoxelShape getShape(@NotNull BlockState state,
	                                              @NotNull BlockGetter level,
	                                              @NotNull BlockPos pos,
	                                              @NotNull CollisionContext context){
		return switch(state.getValue(FACING)){
			case EAST -> SHAPE_EAST;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			default -> SHAPE_NORTH;
		};
	}

	@SuppressWarnings("deprecation")
	@Override @NotNull public InteractionResult use(@NotNull BlockState state,
	                                                @NotNull Level level,
	                                                @NotNull BlockPos pos,
	                                                @NotNull Player player,
	                                                @NotNull InteractionHand hand,
	                                                @NotNull BlockHitResult hit){
		if(!level.isClientSide){
			BargainHandler.initiate(
					player,
					ParagliderBargainTypes.GODDESS_STATUE,
					pos,
					ParagliderAdvancements.PRAY_TO_THE_GODDESS,
					new Vec3(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5));
		}
		return InteractionResult.SUCCESS;
	}

	@Override public void appendHoverText(@NotNull ItemStack stack,
	                                      @Nullable BlockGetter level,
	                                      @NotNull List<Component> tooltip,
	                                      @NotNull TooltipFlag flag){
		if(this.tooltip!=null) tooltip.add(this.tooltip);
	}
}
