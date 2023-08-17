package tictim.paraglider.client;

import net.minecraft.util.FastColor.ARGB32;

public final class StaminaWheelConstants{
	private StaminaWheelConstants(){}

	public static final int IDLE = ARGB32.color(255, 0, 223, 83);
	public static final int GLOW = ARGB32.color(255, 255, 255, 255);
	public static final int EVIL_GLOW = ARGB32.color(255, 248, 223, 223);
	public static final int TRANSPARENT = ARGB32.color(0, 255, 255, 255);
	public static final int DEPLETED_1 = ARGB32.color(255, 150, 2, 2);
	public static final int DEPLETED_2 = ARGB32.color(255, 255, 150, 2);
	public static final int EMPTY = ARGB32.color(150, 2, 2, 2);

	public static final long GLOW_FADE_START = 100;
	public static final long GLOW_FADE = 250;
	public static final long FADE_START = 1000;
	public static final long FADE = 100;
	public static final long BLINK = 300;
	public static final long DEPLETED_BLINK = 600;

	public static final int WHEEL_RADIUS = 10;

	/**
	 * Helper functions for calculating color when stamina wheel glows and fades out.
	 */
	public static int getGlowAndFadeColor(long time){
		if(time<GLOW_FADE_START) return GLOW;
		else if(time<GLOW_FADE_START+GLOW_FADE) return ARGB32.lerp((float)(time-GLOW_FADE_START)/GLOW_FADE, GLOW, IDLE);
		else if(time<FADE_START) return IDLE;
		else if(time<FADE_START+FADE) return ARGB32.lerp((float)(time-FADE_START)/FADE, IDLE, TRANSPARENT);
		else return TRANSPARENT;
	}

	public static float cycle(long currentTime, long cycleTime){
		long halfCycle = cycleTime/2;
		return (float)Math.abs(currentTime%cycleTime-halfCycle)/halfCycle;
	}
}
