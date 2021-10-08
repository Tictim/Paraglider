package tictim.paraglider.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.List;

public class AntiVesselItem extends Item{
	public AntiVesselItem(Properties properties){
		super(properties);
	}

	@Override public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide){
			ServerPlayerMovement m = ServerPlayerMovement.of(player);
			if(m!=null){
				int staminaVessels = m.getStaminaVessels();
				if(staminaVessels>0){
					m.setStaminaVessels(0);
					m.setStamina(Math.min(m.getStamina(), m.getMaxStamina()));
					ParagliderUtils.giveItem(player, new ItemStack(Contents.STAMINA_VESSEL.get(), staminaVessels));
				}
				int heartContainers = m.getHeartContainers();
				if(heartContainers>0){
					m.setHeartContainers(0);
					ParagliderUtils.giveItem(player, new ItemStack(Contents.HEART_CONTAINER.get(), heartContainers));
				}
				if(staminaVessels>0||heartContainers>0){
					stack.shrink(1);
					return InteractionResultHolder.consume(stack);
				}
			}
		}
		return InteractionResultHolder.success(stack);
	}

	@Override public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tooltip.paraglider.anti_vessel.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
		tooltip.add(new TranslatableComponent("tooltip.paraglider.anti_vessel.1")
				.setStyle(Style.EMPTY));
	}

	@Override public boolean isFoil(ItemStack stack){
		return true;
	}
}
