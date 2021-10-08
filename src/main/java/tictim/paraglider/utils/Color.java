package tictim.paraglider.utils;

import net.minecraft.util.Mth;

import java.util.Objects;

public final class Color{
	public static Color of(int red, int green, int blue){
		return new Color(Mth.clamp(red, 0, 255)/255.0f,
				Mth.clamp(green, 0, 255)/255.0f,
				Mth.clamp(blue, 0, 255)/255.0f);
	}
	public static Color of(int red, int green, int blue, int alpha){
		return new Color(Mth.clamp(red, 0, 255)/255.0f,
				Mth.clamp(green, 0, 255)/255.0f,
				Mth.clamp(blue, 0, 255)/255.0f,
				Mth.clamp(alpha, 0, 255)/255.0f);
	}

	public final float red;
	public final float green;
	public final float blue;
	public final float alpha;

	public Color(float red, float green, float blue){
		this(red, green, blue, 1);
	}
	public Color(float red, float green, float blue, float alpha){
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public Color blend(Color color2, float blend){
		return new Color(red+((color2.red-red)*blend),
				green+((color2.green-green)*blend),
				blue+((color2.blue-blue)*blend),
				alpha+((color2.alpha-alpha)*blend));
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		Color color = (Color)o;
		return Double.compare(color.red, red)==0&&
				Double.compare(color.green, green)==0&&
				Double.compare(color.blue, blue)==0&&
				Double.compare(color.alpha, alpha)==0;
	}
	@Override public int hashCode(){
		return Objects.hash(red, green, blue, alpha);
	}

	@Override public String toString(){
		return String.format("[R: %f, G: %f, B: %f, A: %f]", red, green, blue, alpha);
	}
}
