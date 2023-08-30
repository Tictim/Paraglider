package tictim.paraglider.contents.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.vessel.VesselContainer;

public abstract class VesselItem extends Item{
	public VesselItem(@NotNull Properties properties){
		super(properties);
	}

	@Override public boolean isFoil(@NotNull ItemStack stack){
		return true;
	}

	@Override @NotNull public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		VesselContainer vessels = VesselContainer.get(player);
		if(give(vessels, true, false)){
			if(!level.isClientSide){
				give(vessels, false, true);
				stack.shrink(1);
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
		}else return InteractionResultHolder.fail(stack);
	}

	protected abstract boolean give(VesselContainer vessels, boolean simulate, boolean playEffect);
}
