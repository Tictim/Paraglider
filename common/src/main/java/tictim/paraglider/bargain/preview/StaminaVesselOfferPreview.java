package tictim.paraglider.bargain.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.bargain.OfferPreview;
import tictim.paraglider.contents.Contents;

import java.util.List;

public final class StaminaVesselOfferPreview implements OfferPreview{
	private final int quantity;
	private final ItemStack preview;

	public StaminaVesselOfferPreview(int quantity){
		this.quantity = quantity;
		this.preview = new ItemStack(Contents.get().staminaVessel());
	}

	@Override @NotNull public ItemStack preview(){
		return preview;
	}

	@Override public int quantity(){
		return quantity;
	}

	@Environment(EnvType.CLIENT)
	@Override @NotNull public List<@NotNull Component> getTooltip(){
		return List.of(quantity==1 ?
				Component.translatable("bargain.paraglider.heart_container") :
				Component.translatable("bargain.paraglider.heart_container.s", quantity));
	}
}
