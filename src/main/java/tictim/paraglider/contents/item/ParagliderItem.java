package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ParagliderItem extends Item {
	public ParagliderItem(Properties p) {
		super(p);
	}

	public ParagliderItem() {
		this(new Properties().stacksTo(1));
	}

	@SuppressWarnings("deprecation")
	@Override public void appendHoverText(
			@NotNull ItemStack stack, @NotNull TooltipContext context,
			@NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
			@NotNull TooltipFlag flag) {
		if (stack.isDamageableItem() && stack.getMaxDamage() <= stack.getDamageValue()) {
			tooltipAdder.accept(Component.translatable("tooltip.paraglider.paraglider_broken")
					.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
		}
	}
}
