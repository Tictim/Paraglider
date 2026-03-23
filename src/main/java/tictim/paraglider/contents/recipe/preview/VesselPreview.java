package tictim.paraglider.contents.recipe.preview;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.VesselSlotDisplay;

import java.util.List;
import java.util.Locale;

@NullMarked
public record VesselPreview(
		VesselPreview.VesselType vesselType,
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

	@Override public SlotDisplay display() {
		return this.vesselType.slotDisplay;
	}

	@Override public List<Component> getTooltip() {
		return List.of(this.quantity == 1 ?
				Component.translatable(this.vesselType.singleTooltipKey) :
				Component.translatable(this.vesselType.multiTooltipKey, this.quantity));
	}

	@Override public Type<VesselPreview> type() {
		return TYPE;
	}

	public enum VesselType implements StringRepresentable {
		HEART_CONTAINER(VesselSlotDisplay.HEART_CONTAINER, "bargain.paraglider.heart_container"),
		STAMINA_VESSEL(VesselSlotDisplay.STAMINA_VESSEL, "bargain.paraglider.stamina_vessel"),
		ESSENCE(VesselSlotDisplay.ESSENCE, "bargain.paraglider.essence");

		private final String id = name().toLowerCase(Locale.ROOT);
		private final SlotDisplay slotDisplay;
		private final String singleTooltipKey;
		private final String multiTooltipKey;

		VesselType(SlotDisplay slotDisplay, String singleTooltipKey) {
			this.slotDisplay = slotDisplay;
			this.singleTooltipKey = singleTooltipKey;
			this.multiTooltipKey = singleTooltipKey + ".s";
		}

		@Override public String getSerializedName() {
			return this.id;
		}
	}
}
