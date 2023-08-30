package tictim.paraglider.api.bargain;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface DemandPreview{
	@NotNull @Unmodifiable List<@NotNull ItemStack> preview();
	int quantity();
	int count(@NotNull Player player);

	@Environment(EnvType.CLIENT)
	@NotNull List<@NotNull Component> getTooltip(int previewIndex);
}
