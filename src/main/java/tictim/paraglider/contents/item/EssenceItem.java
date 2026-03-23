package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.vessel.VesselContainer;

import java.util.function.Consumer;

@NullMarked
public class EssenceItem extends VesselItem {
	public EssenceItem(Properties properties) {
		super(properties);
	}

	@Override protected boolean give(VesselContainer vessels, boolean simulate, boolean playEffect) {
		return vessels.giveEssences(1, simulate, playEffect) == 1;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			ItemStack stack, TooltipContext context,
			TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder,
			TooltipFlag flag) {
		tooltipAdder.accept(Component.translatable("tooltip.paraglider.essence.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	}
}
