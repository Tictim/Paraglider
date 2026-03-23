package tictim.paraglider.client.settings;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;

import java.util.Locale;

@NullMarked
public enum ExtraWheelAttachment implements StringRepresentable {
	LEFT,
	RIGHT,
	TOP,
	BOTTOM;

	private final String serializedName = name().toLowerCase(Locale.ROOT);
	private final Identifier buttonIconPath = ParagliderAPI.id("icon/extra_wheel_attachment_" + serializedName);

	@Override public String getSerializedName() {
		return this.serializedName;
	}

	public Identifier buttonIconPath() {
		return buttonIconPath;
	}
}
