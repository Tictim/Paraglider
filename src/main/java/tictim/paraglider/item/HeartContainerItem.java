package tictim.paraglider.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.List;

public class HeartContainerItem extends Item{
	public HeartContainerItem(){
		super(new Properties().rarity(Rarity.RARE).tab(Contents.GROUP));
	}

	@Override public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn){
		ItemStack stack = player.getItemInHand(handIn);
		if(ParagliderUtils.giveHeartContainers(player, 1, false, true)){
			if(!world.isClientSide) stack.shrink(1);
			return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
		}else return InteractionResultHolder.fail(stack);
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tooltip.paraglider.heart_container.1",
				new TranslatableComponent("tooltip.paraglider.heart_container.1.hearts").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
				new TextComponent(Integer.toString(ModCfg.maxHeartContainers())).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
		).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
	}
}
