package tictim.paraglider.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.List;

public class HeartContainerItem extends Item{
	public HeartContainerItem(){
		super(new Properties().rarity(Rarity.RARE).group(Contents.GROUP));
	}

	@Override public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn){
		ItemStack stack = player.getHeldItem(handIn);
		if(ParagliderUtils.giveHeartContainers(player, 1, false, true)){
			if(!world.isRemote) stack.shrink(1);
			return ActionResult.func_233538_a_(stack, world.isRemote);
		}else return ActionResult.resultFail(stack);
	}

	@Override public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.paraglider.heart_container.1",
				new TranslationTextComponent("tooltip.paraglider.heart_container.1.hearts").setStyle(Style.EMPTY.setFormatting(TextFormatting.YELLOW)),
				new StringTextComponent(Integer.toString(ModCfg.maxHeartContainers())).setStyle(Style.EMPTY.setFormatting(TextFormatting.YELLOW))
		).setStyle(Style.EMPTY.setFormatting(TextFormatting.GREEN)));
	}
}
