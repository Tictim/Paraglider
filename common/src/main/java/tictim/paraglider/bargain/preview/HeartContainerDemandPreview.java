package tictim.paraglider.bargain.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.bargain.DemandPreview;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.contents.Contents;

import java.util.List;

public final class HeartContainerDemandPreview implements DemandPreview{
	private final int quantity;
	private final List<ItemStack> preview;

	public HeartContainerDemandPreview(int quantity){
		this.quantity = quantity;
		this.preview = List.of(new ItemStack(Contents.get().heartContainer()));
	}

	@Override @NotNull @Unmodifiable public List<@NotNull ItemStack> preview(){
		return preview;
	}
	@Override public int quantity(){
		return quantity;
	}

	@Override public int count(@NotNull Player player){
		return VesselContainer.get(player).heartContainer();
	}

	@Environment(EnvType.CLIENT)
	@Override @NotNull public List<@NotNull Component> getTooltip(int previewIndex){
		return List.of(quantity==1 ?
				Component.translatable("bargain.paraglider.heart_container") :
				Component.translatable("bargain.paraglider.heart_container.s", quantity));
	}
}
