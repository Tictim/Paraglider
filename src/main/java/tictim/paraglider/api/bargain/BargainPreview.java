package tictim.paraglider.api.bargain;

import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;

import java.util.List;

@NullMarked
public interface BargainPreview<T extends BargainPreview<T>> {
	ResourceKey<Registry<Type<?>>> TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(ParagliderAPI.id("bargain_preview_type"));

	/**
	 * @return Slot display of this preview
	 */
	SlotDisplay display();
	/**
	 * @return Quantity of this preview
	 */
	int quantity();

	/**
	 * @return Custom tooltip, will fall back to item stack tooltip if not provided. Called on client-side.
	 */
	default @Nullable List<Component> getTooltip() {
		return null;
	}

	Type<T> type();

	record Type<T extends BargainPreview<T>>(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {}
}
