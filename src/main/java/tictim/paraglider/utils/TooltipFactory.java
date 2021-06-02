package tictim.paraglider.utils;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface TooltipFactory{
	static TooltipFactory heartContainer(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.heart_container")) :
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.heart_container.s", quantity));
	}

	static TooltipFactory staminaVessel(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.stamina_vessel")) :
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.stamina_vessel.s", quantity));
	}

	static TooltipFactory essence(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.essence")) :
				tooltip -> tooltip.add(new TranslationTextComponent("bargain.paraglider.essence.s", quantity));
	}

	void addTooltip(List<ITextComponent> tooltip);

	default List<ITextComponent> getTooltip(){
		List<ITextComponent> list = new ArrayList<>();
		addTooltip(list);
		return list;
	}
}
