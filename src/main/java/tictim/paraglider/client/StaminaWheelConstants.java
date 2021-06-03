package tictim.paraglider.client;

import tictim.paraglider.utils.Color;

public final class StaminaWheelConstants{
	private StaminaWheelConstants(){}

	public static final Color IDLE = Color.of(0, 223, 83);
	public static final Color GLOW = Color.of(255, 255, 255);
	public static final Color EVIL_GLOW = Color.of(248, 223, 223);
	public static final Color TRANSPARENT = Color.of(255, 255, 255, 0);
	public static final Color DEPLETED_1 = Color.of(150, 2, 2);
	public static final Color DEPLETED_2 = Color.of(255, 150, 2);
	public static final Color EMPTY = Color.of(2, 2, 2, 150);

	public static final long GLOW_FADE_START = 100;
	public static final long GLOW_FADE = 250;
	public static final long FADE_START = 1000;
	public static final long FADE = 100;
	public static final long BLINK = 300;
	public static final long DEPLETED_BLINK = 600;

	public static final double WHEEL_SIZE = 10;

	/**
	 * Helper functions for calculating color when stamina wheel glows and fades out.
	 */
	public static Color getGlowAndFadeColor(long time){
		if(time<GLOW_FADE_START)
			return GLOW;
		else if(time<GLOW_FADE_START+GLOW_FADE)
			return GLOW.blend(IDLE, (float)(time-GLOW_FADE_START)/GLOW_FADE);
		else if(time<FADE_START)
			return IDLE;
		else if(time<FADE_START+FADE)
			return IDLE.blend(TRANSPARENT, (float)(time-FADE_START)/FADE);
		else return TRANSPARENT;
	}

	public static float cycle(long currentTime, long cycleTime){
		long halfCycle = cycleTime/2;
		return (float)Math.abs(currentTime%cycleTime-halfCycle)/halfCycle;
	}
}
