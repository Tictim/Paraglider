package tictim.paraglider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tictim.paraglider.contents.ModContainers;

import javax.annotation.Nullable;
import java.util.List;

public class HornedStatueBlock extends HorizontalBlock{
	private static final VoxelShape SHAPE_NORTH = VoxelShapes.or(makeCuboidShape(3, 0, 3, 13, 14, 12),
			makeCuboidShape(5, 0, 2, 11, 12, 3),
			makeCuboidShape(3, 0, 12, 13, 13, 13)).simplify();
	private static final VoxelShape SHAPE_EAST = VoxelShapes.or(makeCuboidShape(4, 0, 3, 13, 14, 13),
			makeCuboidShape(13, 0, 5, 14, 12, 11),
			makeCuboidShape(3, 0, 3, 4, 13, 13)).simplify();
	private static final VoxelShape SHAPE_SOUTH = VoxelShapes.or(makeCuboidShape(3, 0, 4, 13, 14, 13),
			makeCuboidShape(5, 0, 13, 11, 12, 14),
			makeCuboidShape(3, 0, 3, 13, 13, 4)).simplify();
	private static final VoxelShape SHAPE_WEST = VoxelShapes.or(makeCuboidShape(3, 0, 3, 12, 14, 13),
			makeCuboidShape(2, 0, 5, 3, 12, 11),
			makeCuboidShape(12, 0, 3, 13, 13, 13)).simplify();

	public HornedStatueBlock(Properties properties){
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
		if(!world.isRemote) ModContainers.openContainer(player, ModContainers::hornedStatue, pos.getX()+0.5f, pos.getY()+0.5f, pos.getZ()+0.5f);
		return ActionResultType.SUCCESS;
	}

	@Override public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.paraglider.horned_statue.0")
				.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)));
	}
}
