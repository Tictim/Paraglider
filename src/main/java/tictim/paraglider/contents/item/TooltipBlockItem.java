package tictim.paraglider.contents.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

@NullMarked
public class TooltipBlockItem extends BlockItem {
	private final Component tooltip;

	public TooltipBlockItem(Block block, Properties properties, Component tooltip) {
		super(block, properties);
		this.tooltip = tooltip;
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			ItemStack stack, TooltipContext context,
			TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder,
			TooltipFlag flag) {
		tooltipAdder.accept(this.tooltip);
	}
}
