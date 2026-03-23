package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.contents.Contents;

import java.util.function.Consumer;

@NullMarked
public class AntiVesselItem extends Item {
	public AntiVesselItem(Properties properties) {
		super(properties);
	}

	@Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			VesselContainer vessels = VesselContainer.get(player);

			int heartContainersTaken = vessels.takeHeartContainers(Integer.MAX_VALUE, false, true);
			int staminaVesselsTaken = vessels.takeStaminaVessels(Integer.MAX_VALUE, false, true);
			if (heartContainersTaken > 0 || staminaVesselsTaken > 0) {
				if (heartContainersTaken > 0) {
					ParagliderUtils.giveItem(player, Contents.get().heartContainer(), heartContainersTaken);
				}
				if (staminaVesselsTaken > 0) {
					ParagliderUtils.giveItem(player, Contents.get().staminaVessel(), staminaVesselsTaken);
				}
				stack.shrink(1);
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			ItemStack stack, TooltipContext context,
			TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder,
			TooltipFlag flag) {
		tooltipAdder.accept(Component.translatable("tooltip.paraglider.anti_vessel.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
	}

	@Override public boolean isFoil(ItemStack stack) {
		return true;
	}
}
