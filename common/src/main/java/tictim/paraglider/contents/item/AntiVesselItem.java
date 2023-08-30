package tictim.paraglider.contents.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.contents.Contents;

import java.util.List;

public class AntiVesselItem extends Item{
	public AntiVesselItem(@NotNull Properties properties){
		super(properties);
	}

	@Override @NotNull public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		if(!level.isClientSide){
			VesselContainer vessels = VesselContainer.get(player);

			int heartContainersTaken = vessels.takeHeartContainers(Integer.MAX_VALUE, false, true);
			int staminaVesselsTaken = vessels.takeStaminaVessels(Integer.MAX_VALUE, false, true);
			if(heartContainersTaken>0||staminaVesselsTaken>0){
				if(heartContainersTaken>0){
					ParagliderUtils.giveItem(player, new ItemStack(Contents.get().heartContainer(), heartContainersTaken));
				}
				if(staminaVesselsTaken>0){
					ParagliderUtils.giveItem(player, new ItemStack(Contents.get().staminaVessel(), staminaVesselsTaken));
				}
				stack.shrink(1);
				return InteractionResultHolder.consume(stack);
			}
		}
		return InteractionResultHolder.success(stack);
	}

	@Override public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag){
		tooltip.add(Component.translatable("tooltip.paraglider.anti_vessel.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
		tooltip.add(Component.translatable("tooltip.paraglider.anti_vessel.1")
				.setStyle(Style.EMPTY));
	}

	@Override public boolean isFoil(@NotNull ItemStack stack){
		return true;
	}
}
