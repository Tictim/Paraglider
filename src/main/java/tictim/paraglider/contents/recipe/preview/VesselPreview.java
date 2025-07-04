package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.Contents;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public record VesselPreview(
		@NotNull VesselPreview.VesselType vesselType,
		int quantity
) implements BargainPreview<VesselPreview> {
	public static final Type<VesselPreview> TYPE = new Type<VesselPreview>(StreamCodec.of(
			(buffer, vesselPreview) -> {
				buffer.writeEnum(vesselPreview.vesselType);
				buffer.writeVarInt(vesselPreview.quantity);
			},
			buffer -> new VesselPreview(
					buffer.readEnum(VesselType.class),
					buffer.readVarInt())
	));

	@Override public @NotNull List<ItemStack> display() {
		return this.vesselType.display();
	}

	@Override public @NotNull List<@NotNull Component> getTooltip() {
		return List.of(this.quantity == 1 ?
				Component.translatable(this.vesselType.singleTooltipKey) :
				Component.translatable(this.vesselType.multiTooltipKey, this.quantity));
	}

	@Override public @NotNull Type<VesselPreview> type() {
		return TYPE;
	}

	public enum VesselType implements StringRepresentable {
		HEART_CONTAINER(() -> List.of(new ItemStack(Contents.get().heartContainer())), "bargain.paraglider.heart_container"),
		STAMINA_VESSEL(() -> List.of(new ItemStack(Contents.get().staminaVessel())), "bargain.paraglider.stamina_vessel"),
		ESSENCE(() -> List.of(new ItemStack(Contents.get().essence())), "bargain.paraglider.essence");

		private final String id = name().toLowerCase(Locale.ROOT);
		private final Supplier<List<ItemStack>> display;
		private final String singleTooltipKey;
		private final String multiTooltipKey;

		private @Nullable List<ItemStack> displayCache;

		VesselType(Supplier<List<ItemStack>> display, String singleTooltipKey) {
			this.display = display;
			this.singleTooltipKey = singleTooltipKey;
			this.multiTooltipKey = singleTooltipKey + ".s";
		}

		public List<ItemStack> display() {
			if (this.displayCache == null) this.displayCache = this.display.get();
			return this.displayCache;
		}

		@Override public @NotNull String getSerializedName() {
			return this.id;
		}
	}
}
