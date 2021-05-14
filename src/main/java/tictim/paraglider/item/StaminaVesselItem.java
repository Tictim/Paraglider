package tictim.paraglider.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.List;

public class StaminaVesselItem extends Item{
	public StaminaVesselItem(){
		super(new Properties().rarity(Rarity.RARE).group(Contents.GROUP));
	}

	@Override public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn){
		ItemStack stack = player.getHeldItem(handIn);
		PlayerMovement h = player.getCapability(PlayerMovement.CAP).orElse(null);
		if(h!=null){
			int vessels = h.getStaminaVessels();
			if(vessels<PlayerMovement.MAX_STAMINA_VESSELS){
				if(!world.isRemote){
					h.setStaminaVessels(vessels+1);
					stack.shrink(1);
				}
				return ActionResult.resultConsume(stack);
			}
		}
		return ActionResult.resultFail(stack);
	}

	@Override public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.stamina_vessel.1",
				new StringTextComponent(Integer.toString(PlayerMovement.STAMINA_INCREMENT)).setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.YELLOW))),
				new StringTextComponent(Integer.toString(PlayerMovement.MAX_STAMINA_VESSELS)).setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.YELLOW)))
		).setStyle(Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.GREEN))));
	}
}
