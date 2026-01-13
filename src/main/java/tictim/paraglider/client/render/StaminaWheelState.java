package tictim.paraglider.client.render;

import it.unimi.dsi.fastutil.floats.Float2IntRBTreeMap;
import it.unimi.dsi.fastutil.floats.Float2IntSortedMap;
import it.unimi.dsi.fastutil.floats.Float2IntSortedMaps;
import net.minecraft.util.ARGB;

import static tictim.paraglider.client.render.StaminaWheelConstants.toWheelPos;

public class StaminaWheelState {
	private final Float2IntRBTreeMap segments = new Float2IntRBTreeMap();
	private double stamina;
	private double maxStamina;
	private int indicatorColor;
	private float alpha = 1;

	public double stamina() {
		return stamina;
	}

	public double maxStamina() {
		return this.maxStamina;
	}

	public void setProperties(double stamina, double maxStamina) {
		this.stamina = stamina;
		this.maxStamina = maxStamina;
	}

	public int indicatorColor() {
		return this.indicatorColor;
	}

	public int indicatorColorWithAlpha() {
		return ARGB.color(this.alpha * ARGB.alphaFloat(this.indicatorColor), this.indicatorColor);
	}

	public void setIndicatorColor(int indicatorColor) {
		this.indicatorColor = indicatorColor;
	}

	public float alpha() {
		return this.alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float staminaWheelPos() {
		return toWheelPos((int) Math.min(maxStamina(), stamina()));
	}

	public Float2IntSortedMap segments() {
		return Float2IntSortedMaps.unmodifiable(this.segments);
	}

	public void fillStamina(double from, double to, int color) {
		fillWheel(toWheelPos(Math.clamp(from, 0, this.maxStamina)),
				toWheelPos(Math.clamp(to, 0, this.maxStamina)), color);
	}

	public void fillWheel(float from, float to, int color) {
		from = Math.max(0, from);
		if (from < to) {
			this.segments.computeIfAbsent(to, to_ -> {
				var last = this.segments.headMap(to_).lastEntry();
				return last == null ? 0 : last.getValue();
			});
			this.segments.subMap(from, to).clear();
			this.segments.put(from, color);
		}
	}

	public void reset() {
		this.segments.clear();
		this.stamina = 0;
		this.maxStamina = 0;
		this.indicatorColor = 0;
		this.alpha = 1;
	}
}
