package tictim.paraglider.block;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class GoronGoddessStatueBlock extends GoddessStatueBlock{
	public GoronGoddessStatueBlock(Properties properties){
		super(properties);
	}

	@Override public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tooltip.paraglider.goron_goddess_statue.0")
				.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)));
	}
}
