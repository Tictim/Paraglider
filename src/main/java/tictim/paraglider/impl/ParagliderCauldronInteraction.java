package tictim.paraglider.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class ParagliderCauldronInteraction implements CauldronInteraction {
	public static final ParagliderCauldronInteraction INSTANCE = new ParagliderCauldronInteraction();

	@Override
	public @NotNull InteractionResult interact(@NotNull BlockState state, @NotNull Level level,
	                                           @NotNull BlockPos pos, @NotNull Player player,
	                                           @NotNull InteractionHand hand, @NotNull ItemStack stack) {
		if (!stack.is(ItemTags.DYEABLE)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		} else if (!stack.has(DataComponents.DYED_COLOR)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		} else {
			if (!level.isClientSide) {
				stack.remove(DataComponents.DYED_COLOR);
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}
			return InteractionResult.SUCCESS;
		}
	}
}
