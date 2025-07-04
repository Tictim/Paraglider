package tictim.paraglider.client.settings;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ExtraWheelAttachment implements StringRepresentable {
	LEFT,
	RIGHT,
	TOP,
	BOTTOM;

	private final String serializedName = name().toLowerCase(Locale.ROOT);

	@Override public @NotNull String getSerializedName() {
		return this.serializedName;
	}
}
