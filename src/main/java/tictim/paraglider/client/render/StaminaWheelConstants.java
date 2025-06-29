package tictim.paraglider.client.render;

import net.minecraft.util.ARGB;
import tictim.paraglider.api.stamina.Stamina;

import static net.minecraft.util.ARGB.lerp;

public final class StaminaWheelConstants {
	private StaminaWheelConstants() {}

	public static final int GLOW = ARGB.color(255, 255, 255);
	public static final int EVIL_GLOW = ARGB.color(248, 223, 223);
	public static final int DEPLETED_1 = ARGB.color(150, 2, 2);
	public static final int DEPLETED_2 = ARGB.color(255, 150, 2);
	public static final int EMPTY = ARGB.color(150, 2, 2, 2);

	public static final long GLOW_FADE_START = 100;
	public static final long GLOW_FADE_DURATION = 250;
	public static final long GLOW_FADE_END = GLOW_FADE_START + GLOW_FADE_DURATION;
	public static final long FADE_START = 1000;
	public static final long FADE_DURATION = 100;
	public static final long FADE_END = FADE_START + FADE_DURATION;

	public static final long BLINK = 300;
	public static final long DEPLETED_BLINK = 600;

	public static final long EXTRA_WHEEL_FILL_DURATION = 250;
	public static final long EXTRA_WHEEL_EMPTY_DURATION = 250;

	public static final int WHEEL_RADIUS = 10;

	// pair of idle/background colors for stamina - first is the basic green color
	// later ones are used for 4th wheel and beyond, cycling through each entry
	private static final int[] WHEEL_COLORS = {
			0xff00df53, 0xff006b24,
			0xff8ab7ff, 0xff1457a2,
			0xffff85ac, 0xff922e51,
			0xffcfbf00, 0xff5f5c00,
			0xff00d2ff, 0xff006480,
			0xffff79ff, 0xff7e377f,
			0xffeeaa00, 0xff784d00,
			0xff00ddc6, 0xff006a5e,
			0xffc0a2ff, 0xff5a469d,
			0xffff9060, 0xff94350f,
	};

	public static int wheelColor(int offset) {
		return WHEEL_COLORS[offset % (WHEEL_COLORS.length / 2) * 2];
	}

	public static int wheelBgColor(int offset) {
		return WHEEL_COLORS[offset % (WHEEL_COLORS.length / 2) * 2 + 1];
	}

	public static int getGlowAndFadeColor(long time, int baseColor) {
		if (time < GLOW_FADE_START) return GLOW;
		if (time < GLOW_FADE_END) return lerp(
				(float)(time - GLOW_FADE_START) / GLOW_FADE_DURATION, GLOW, baseColor);
		if (time < FADE_START) return baseColor;
		if (time < FADE_END) return lerp(
				(float)(time - FADE_START) / FADE_DURATION, baseColor, ARGB.color(0, baseColor));
		return 0;
	}

	public static int getGlowColor(long time, int baseColor) {
		if (time < GLOW_FADE_START) return GLOW;
		if (time < GLOW_FADE_END) return lerp(
				(float)(time - GLOW_FADE_START) / GLOW_FADE_DURATION, GLOW, baseColor);
		return baseColor;
	}

	public static int getBlinkColor(long time, boolean depleted) {
		return ARGB.lerp(cycle(time, depleted ? DEPLETED_BLINK : BLINK), DEPLETED_1, DEPLETED_2);
	}

	public static float cycle(long currentTime, long cycleTime) {
		long halfCycle = cycleTime / 2;
		return (float)Math.abs(currentTime % cycleTime - halfCycle) / halfCycle;
	}

	public static float toWheelPos(double stamina) {
		return (float)(stamina / Stamina.STAMINA_PER_WHEEL);
	}
}
