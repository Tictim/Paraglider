package tictim.paraglider.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public sealed interface StaminaWheelPosition {
	StaminaWheelPosition DEFAULT = new ScreenProportion((427 - 100) / 854.0, (240 - 15) / 480.0);

	double x(double width);
	double y(double height);

	record ScreenProportion(
			double x, double y
	) implements StaminaWheelPosition {
		public static final MapCodec<ScreenProportion> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
				Codec.DOUBLE.fieldOf("x").forGetter(ScreenProportion::x),
				Codec.DOUBLE.fieldOf("y").forGetter(ScreenProportion::y)
		).apply(b, ScreenProportion::new));

		@Override public double x(double width) {
			return width * this.x;
		}

		@Override public double y(double height) {
			return height * this.y;
		}
	}

	record Anchored(
			Dir8 anchor,
			double x,
			double y
	) implements StaminaWheelPosition {
		public static final MapCodec<Anchored> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
				StringRepresentable.fromValues(Dir8::values).fieldOf("anchor").forGetter(Anchored::anchor),
				Codec.DOUBLE.fieldOf("x").forGetter(Anchored::x),
				Codec.DOUBLE.fieldOf("y").forGetter(Anchored::y)
		).apply(b, Anchored::new));

		@Override public double x(double width) {
			return this.anchor.anchorX(width) + this.x;
		}
		@Override public double y(double height) {
			return this.anchor.anchorY(height) + this.y;
		}
	}

	enum Dir8 implements StringRepresentable {
		U, UR, R, DR, D, DL, L, UL;

		private final String serializedName = name().toLowerCase(Locale.ROOT);

		public double anchorX(double width) {
			return switch (this) {
				case DL, L, UL -> 0;
				case U, D -> width / 2.0;
				case UR, R, DR -> width;
			};
		}

		public double anchorY(double height) {
			return switch (this) {
				case UL, U, UR -> 0;
				case R, L -> height / 2.0;
				case DL, D, DR -> height;
			};
		}

		@Override public @NotNull String getSerializedName() {
			return this.serializedName;
		}
	}
}
