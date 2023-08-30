package tictim.paraglider.contents.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

	public HornedStatueBlock(@NotNull Properties properties){
		super(properties);
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
					ParagliderBargainTypes.HORNED_STATUE,
					pos,
					ParagliderAdvancements.STATUES_BARGAIN,
					new Vec3(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5));
		}
		return InteractionResult.SUCCESS;
	}

	@Override public void appendHoverText(@NotNull ItemStack stack,
	                                      @Nullable BlockGetter level,
	                                      @NotNull List<Component> tooltip,
	                                      @NotNull TooltipFlag flag){
		tooltip.add(Component.translatable("tooltip.paraglider.horned_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	}
}
