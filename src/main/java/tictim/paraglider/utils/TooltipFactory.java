package tictim.paraglider.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface TooltipFactory{
	static TooltipFactory heartContainer(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.heart_container")) :
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.heart_container.s", quantity));
	}

	static TooltipFactory staminaVessel(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.stamina_vessel")) :
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.stamina_vessel.s", quantity));
	}

	static TooltipFactory essence(int quantity){
		return quantity==1 ?
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.essence")) :
				tooltip -> tooltip.add(new TranslatableComponent("bargain.paraglider.essence.s", quantity));
	}

	void addTooltip(List<Component> tooltip);

	default List<Component> getTooltip(){
		List<Component> list = new ArrayList<>();
		addTooltip(list);
		return list;
	}
}
