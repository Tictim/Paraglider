package tictim.paraglider.contents.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.vessel.VesselContainer;

@NullMarked
public abstract class VesselItem extends Item {
	public VesselItem(Properties properties) {
		super(properties);
	}

	@Override public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		VesselContainer vessels = VesselContainer.get(player);
		if (give(vessels, true, false)) {
			if (!level.isClientSide()) {
				give(vessels, false, true);
				stack.shrink(1);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.FAIL;
	}

	protected abstract boolean give(VesselContainer vessels, boolean simulate, boolean playEffect);
}
