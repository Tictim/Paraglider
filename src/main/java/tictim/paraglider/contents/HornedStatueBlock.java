package tictim.paraglider.contents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class HornedStatueBlock extends Block{
	private static final VoxelShape SHAPE = Block.makeCuboidShape(3, 0, 3, 13, 14, 13);

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
		return SHAPE;
	}

	@SuppressWarnings("deprecation") @Override public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!world.isRemote){
			PlayerMovement h = player.getCapability(PlayerMovement.CAP).orElse(null);
			if(h!=null){
				int staminaVessels = h.getStaminaVessels();
				if(staminaVessels>0){
					h.setStaminaVessels(0);
					h.setStamina(Math.min(h.getStamina(), PlayerMovement.BASE_STAMINA));
					for(int i = 0; i<staminaVessels; i++){
						InventoryHelper.spawnItemStack(world, player.getPosX(), player.getPosY(), player.getPosZ(), new ItemStack(Contents.STAMINA_VESSEL.get()));
					}
				}
				int heartContainers = h.getHeartContainers();
				if(heartContainers>0){
					h.setHeartContainers(0);
					for(int i = 0; i<heartContainers; i++){
						InventoryHelper.spawnItemStack(world, player.getPosX(), player.getPosY(), player.getPosZ(), new ItemStack(Contents.HEART_CONTAINER.get()));
					}
				}
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.horned_statue.1").setStyle(new Style().setColor(TextFormatting.GREEN)));
		tooltip.add(new TranslationTextComponent("tooltip.horned_statue.2").setStyle(new Style().setColor(TextFormatting.YELLOW).setItalic(true)));
	}
}
