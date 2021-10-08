package tictim.paraglider.block;

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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tictim.paraglider.contents.ModContainers;

import javax.annotation.Nullable;
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

	public GoddessStatueBlock(Properties properties){
		this(properties, null);
	}
	public GoddessStatueBlock(Properties properties, @Nullable Component tooltip){
		super(properties);
		this.tooltip = tooltip;
	}

	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(FACING);
	}

	@Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@SuppressWarnings("deprecation") @Override public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return switch(state.getValue(FACING)){
			case EAST -> SHAPE_EAST;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			default -> // case NORTH:
					SHAPE_NORTH;
		};
	}

	@SuppressWarnings("deprecation") @Override public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
		if(!world.isClientSide) ModContainers.openContainer(player, ModContainers::goddessStatue, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5);
		return InteractionResult.SUCCESS;
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		if(this.tooltip!=null) tooltip.add(this.tooltip);
	}
}
