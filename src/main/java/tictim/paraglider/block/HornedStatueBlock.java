package tictim.paraglider.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
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

public class HornedStatueBlock extends HorizontalDirectionalBlock{
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

	public HornedStatueBlock(Properties properties){
		super(properties);
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
		if(!world.isClientSide) ModContainers.openContainer(player, ModContainers::hornedStatue, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
		return InteractionResult.SUCCESS;
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tooltip.paraglider.horned_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	}
}
