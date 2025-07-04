package tictim.paraglider.contents.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TooltipBlockItem extends BlockItem {
	private final Component tooltip;

	public TooltipBlockItem(Block block, Properties properties, @NotNull Component tooltip) {
		super(block, properties);
		this.tooltip = tooltip;
	}

	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
		tooltipComponents.add(this.tooltip);
	}
}
