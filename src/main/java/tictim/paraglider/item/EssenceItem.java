package tictim.paraglider.item;

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
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.List;

public class EssenceItem extends Item{
	public EssenceItem(Properties properties){
		super(properties);
	}

	@Override public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide){
			if(ParagliderUtils.giveEssences(player, 1, false, true)){
				stack.shrink(1);
				return InteractionResultHolder.consume(stack);
			}
		}
		return InteractionResultHolder.success(stack);
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tooltip.paraglider.essence.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
	}
}
