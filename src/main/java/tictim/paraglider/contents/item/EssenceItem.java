package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.vessel.VesselContainer;

import java.util.function.Consumer;

public class EssenceItem extends VesselItem {
	public EssenceItem(@NotNull Properties properties) {
		super(properties);
	}

	@Override protected boolean give(VesselContainer vessels, boolean simulate, boolean playEffect) {
		return vessels.giveEssences(1, simulate, playEffect) == 1;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
			@NotNull TooltipFlag flag) {
		tooltipAdder.accept(Component.translatable("tooltip.paraglider.essence.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	}
}
