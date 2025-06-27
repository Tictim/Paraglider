package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

import java.util.function.Consumer;

public class HeartContainerItem extends VesselItem {
	public HeartContainerItem(@NotNull Properties properties) {
		super(properties);
	}

	@Override protected boolean give(VesselContainer vessels, boolean simulate, boolean playEffect) {
		return vessels.giveHeartContainers(1, simulate, playEffect) == 1;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
			@NotNull TooltipFlag flag) {
		tooltipAdder.accept(Component.translatable("tooltip.paraglider.heart_container.1",
				Component.translatable("tooltip.paraglider.heart_container.1.hearts").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
				Component.literal(Integer.toString(Cfg.get().maxHeartContainers())).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
		).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
	}
}
