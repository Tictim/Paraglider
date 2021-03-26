package tictim.paraglider.client.dialog;

import net.minecraft.util.math.MathHelper;

public final class WidgetUtils{
	private WidgetUtils(){}

	public static float linearOscillation(long t, long halfCycle, float base, float deviation){
		float osc = Math.abs((float)(t%halfCycle)/halfCycle);
		if(t/halfCycle%2!=0) osc = 1-osc;
		return base+deviation*osc;
	}

	/**
	 * Calculates how much portion of time is passed.
	 *
	 * @param since    Starting point of measure.
	 * @param duration Time unit to measure portion.
	 * @return 1 if more than {@code duration} is passed after {@code since}.
	 * 0 if no time is passed or {@code since} is larger than current time.
	 * {@code (current time - since) / duration} if otherwise.
	 */
	public static float pct(long since, long duration){
		long t = System.currentTimeMillis()-since;
		return t<=0 ? 0 : t>=duration ? 1 : (float)t/duration;
	}

	public static int argb(float alpha, int rgb){
		return (MathHelper.clamp(Math.round(alpha*0xFF), 0, 0xFF)<<24)|(rgb&0xFFFFFF);
	}
}
