package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;

import java.util.List;

public class HeartContainerItem extends VesselItem{
	public HeartContainerItem(@NotNull Properties properties){
		super(properties);
	}

	@Override protected boolean give(VesselContainer vessels, boolean simulate, boolean playEffect){
		return vessels.giveHeartContainers(1, simulate, playEffect)==1;
	}

	@Override public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag){
		tooltip.add(Component.translatable("tooltip.paraglider.heart_container.1",
				Component.translatable("tooltip.paraglider.heart_container.1.hearts").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
				Component.literal(Integer.toString(Cfg.get().maxHeartContainers())).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
		).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
	}
}
