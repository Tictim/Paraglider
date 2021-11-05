package tictim.paraglider.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.List;

public class AntiVesselItem extends Item{
	public AntiVesselItem(Properties properties){
		super(properties);
	}

	@Override public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand){
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote){
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
					return ActionResult.resultConsume(stack);
				}
			}
		}
		return ActionResult.resultSuccess(stack);
	}

	@Override public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.paraglider.anti_vessel.0")
				.setStyle(Style.EMPTY.setFormatting(TextFormatting.GREEN)));
		tooltip.add(new TranslationTextComponent("tooltip.paraglider.anti_vessel.1")
				.setStyle(Style.EMPTY));
	}

	@Override public boolean hasEffect(ItemStack stack){
		return true;
	}
}
