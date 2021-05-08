package tictim.paraglider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.contents.Dialogs;
import tictim.paraglider.dialog.Dialog;

import javax.annotation.Nullable;

public class GoddessStatueBlock extends HorizontalBlock{
	private static final VoxelShape SHAPE_NORTH = VoxelShapes.or(makeCuboidShape(4, 16, 5, 12, 20, 11),
			makeCuboidShape(4, 16, 11, 12, 19, 12),
			makeCuboidShape(4, 12, 5, 12, 16, 12),
			makeCuboidShape(5, 16, 4, 11, 19, 5),
			makeCuboidShape(5, 12, 4, 11, 16, 5),
			makeCuboidShape(3, 0, 3, 13, 12, 13)).simplify();
	private static final VoxelShape SHAPE_EAST = VoxelShapes.or(makeCuboidShape(5, 16, 4, 11, 20, 12),
			makeCuboidShape(4, 16, 4, 5, 19, 12),
			makeCuboidShape(4, 12, 4, 11, 16, 12),
			makeCuboidShape(11, 16, 5, 12, 19, 11),
			makeCuboidShape(11, 12, 5, 12, 16, 11),
			makeCuboidShape(3, 0, 3, 13, 12, 13)).simplify();
	private static final VoxelShape SHAPE_SOUTH = VoxelShapes.or(makeCuboidShape(4, 16, 5, 12, 20, 11),
			makeCuboidShape(4, 16, 4, 12, 19, 5),
			makeCuboidShape(4, 12, 4, 12, 16, 11),
			makeCuboidShape(5, 16, 11, 11, 19, 12),
			makeCuboidShape(5, 12, 11, 11, 16, 12),
			makeCuboidShape(3, 0, 3, 13, 12, 13)).simplify();
	private static final VoxelShape SHAPE_WEST = VoxelShapes.or(makeCuboidShape(5, 16, 4, 11, 20, 12),
			makeCuboidShape(11, 16, 4, 12, 19, 12),
			makeCuboidShape(5, 12, 4, 12, 16, 12),
			makeCuboidShape(4, 16, 5, 5, 19, 11),
			makeCuboidShape(4, 12, 5, 5, 16, 11),
			makeCuboidShape(3, 0, 3, 13, 12, 13)).simplify();

	public GoddessStatueBlock(Properties properties){
		super(properties);
	}

	@Override protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(HORIZONTAL_FACING);
	}

	@Nullable @Override public BlockState getStateForPlacement(BlockItemUseContext context){
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@SuppressWarnings("deprecation") @Override public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		switch(state.get(HORIZONTAL_FACING)){
			case EAST:
				return SHAPE_EAST;
			case SOUTH:
				return SHAPE_SOUTH;
			case WEST:
				return SHAPE_WEST;
			default: // case NORTH:
				return SHAPE_NORTH;
		}
	}

	@SuppressWarnings("deprecation") @Override public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!world.isRemote){
			Dialog dialog;
			PlayerMovement movement = PlayerMovement.of(player);
			if(movement==null){
				ParagliderMod.LOGGER.warn("Player {} have no PlayerMovement attached", player);
				return ActionResultType.SUCCESS;
			}
			if(movement.getStaminaVessels()<PlayerMovement.MAX_STAMINA_VESSELS||
					movement.getHeartContainers()<PlayerMovement.MAX_HEART_CONTAINERS){
				dialog = Dialogs.GODDESS_STATUE;
			}else dialog = Dialogs.GODDESS_STATUE_FULL;
			player.openContainer(dialog.getContainerProvider(new Vector3f(pos.getX()+0.5f, pos.getY()+1, pos.getZ()+0.5f)));
		}
		return ActionResultType.SUCCESS;
	}
}
