package tictim.paraglider.api.bargain;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface OfferPreview{
	@NotNull ItemStack preview();
	int quantity();

	@Environment(EnvType.CLIENT)
	@NotNull List<@NotNull Component> getTooltip();
}
