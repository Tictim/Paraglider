package tictim.paraglider.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.opengl.GL11;
import tictim.paraglider.capabilities.PlayerMovement;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Math.PI;
import static tictim.paraglider.ParagliderMod.MODID;

public final class StaminaWheelRenderer{
	private final Map<WheelType, Wheel> wheel = new EnumMap<>(WheelType.class);

	@Nullable public Wheel getWheel(WheelType type){
		return wheel.get(type);
	}

	public void addWheel(WheelType wheelType, double start, double end, Color color){
		start = Math.max(0, start);
		end = Math.min(1, end);
		if(start>=end) return;

		Wheel wheel = this.wheel.get(wheelType);
		if(wheel!=null){
			this.wheel.put(wheelType, wheel.insert(new Wheel(start, end, color)));
		}else{
			this.wheel.put(wheelType, new Wheel(start, end, color));
		}
	}

	public void render(MatrixStack stack, double x, double y, double z, double size, boolean debug){
		RenderSystem.disableDepthTest();

		BufferBuilder b = Tessellator.getInstance().getBuffer();
		Minecraft mc = Minecraft.getInstance();

		if(debug){
			//int lines = 0;
			float linePos = 10;
			FontRenderer font = mc.fontRenderer;
			for(WheelType t : WheelType.values()){
				Wheel wheel = getWheel(t);
				if(wheel!=null){
					linePos = font.drawStringWithShadow(stack, t+":", 20, linePos, 0xFFFFFFFF);
					//for(String s : font.getWordWrappedHeight(wheel.toString(), mc.getMainWindow().getScaledWidth()-30)){

					//}
					linePos = font.drawStringWithShadow(stack, wheel.toString(), 30, linePos, 0xFFFFFFFF);
				}
			}
		}

		for(WheelType t : WheelType.values()){
			Wheel wheel = getWheel(t);
			if(wheel!=null){
				mc.getTextureManager().bindTexture(t.texture);
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				wheel.draw(stack, b, x, y, z, size, debug);
			}
		}

		RenderSystem.enableDepthTest();
		wheel.clear();
	}

	public static final class Wheel{
		private double start;
		private double end;
		private final Color color;

		@Nullable private Wheel next;

		private Wheel(double start, double end, Color color){
			this.start = start;
			this.end = end;
			this.color = Objects.requireNonNull(color);
		}

		/**
		 * Inserts another part of wheel into current node.
		 *
		 * @param wheel another part of wheel
		 * @return New starting point of wheel node, may or may not be changed
		 */
		public Wheel insert(Wheel wheel){
			return insert(wheel, true);
		}
		/**
		 * Inserts another part of wheel into current node.
		 *
		 * @param wheel     another part of wheel
		 * @param overwrite whether this wheel sits on top of entire node or not
		 * @return New starting point of wheel node, may or may not be changed
		 */
		private Wheel insert(Wheel wheel, boolean overwrite){
			if(wheel.start<=this.start){ // Overrides this - starting point will be changed
				if(wheel.end>=this.end){ // COMPLETELY overwrites this
					return this.next!=null ? wheel.insert(this.next, false) : wheel;
				}else{
					this.start = wheel.end;
					wheel.next = this;
					return wheel;
				}
			}else{ // Doesn't
				if(overwrite&&wheel.start<this.end) this.end = wheel.start;
				next = next!=null ? next.insert(wheel, overwrite) : wheel;
				return this;
			}
		}

		private static final double[] renderPoints = {0, 1/8.0, 3/8.0, 5/8.0, 7/8.0, 1};

		public void draw(MatrixStack stack, BufferBuilder b, double x, double y, double z, double size, boolean debug){
			List<Vector2f> debugVertices = debug ? new ArrayList<>() : null;
			b.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR_TEX);
			b.pos(x, y, z).color(color.red, color.green, color.blue, color.alpha).tex(0.5f, 0.5f).endVertex();
			drawInternal(b, x, y, z, size, debugVertices, false);
			b.finishDrawing();
			WorldVertexBufferUploader.draw(b);

			if(debugVertices!=null){
				stack.push();
				stack.translate(x, y, z);
				FontRenderer font = Minecraft.getInstance().fontRenderer;
				for(Vector2f vec : debugVertices){
					String s = vec.x+" "+vec.y;
					font.drawStringWithShadow(stack, s,
							vec.x>0 ? vec.x*(float)size+2 : vec.x*(float)size-2-font.getStringWidth(s),
							vec.y>0 ? vec.y*(float)-size-2-font.FONT_HEIGHT : vec.y*(float)-size+2,
							0xFF00FF00);
				}
				stack.pop();
			}
		}
		private void drawInternal(BufferBuilder b, double x, double y, double z, double size, @Nullable List<Vector2f> debugVertices, boolean skipFirst){
			for(int i = 0; i<renderPoints.length-1; i++){
				double currentStart = renderPoints[i];
				if(currentStart>=end) break;
				double currentEnd = renderPoints[i+1];
				if(currentEnd<=start) continue;

				if(currentStart<=start){
					if(!skipFirst) vert(b, x, y, z, start, size, debugVertices);
				}
				vert(b, x, y, z, Math.min(currentEnd, end), size, debugVertices);
			}
			if(next!=null) next.drawInternal(b, x, y, z, size, debugVertices, end==next.start);
		}

		private void vert(BufferBuilder b, double x, double y, double z, double point, double size, @Nullable List<Vector2f> debugVertices){
			double vx, vy;
			if(point==0||point==1){
				vx = 0;
				vy = 1;
			}else if(point==1/8.0){
				vx = -1;
				vy = 1;
			}else if(point==3/8.0){
				vx = -1;
				vy = -1;
			}else if(point==5/8.0){
				vx = 1;
				vy = -1;
			}else if(point==7/8.0){
				vx = 1;
				vy = 1;
			}else if(point<1/8.0||point>7/8.0){
				vx = -Math.tan(point*(2*PI));
				vy = 1;
			}else if(point<3/8.0){
				vx = -1;
				vy = 1/Math.tan(point*(2*PI));
			}else if(point<5/8.0){
				vx = Math.tan(point*(2*PI));
				vy = -1;
			}else{ // point<7/8.0
				vx = 1;
				vy = -1/Math.tan(point*(2*PI));
			}
			b.pos(x+vx*size, y+vy*-size, z).color(color.red, color.green, color.blue, color.alpha).tex((float)(vx/2+0.5), (float)(vy/2+0.5)).endVertex();
			if(debugVertices!=null) debugVertices.add(new Vector2f((float)vx, (float)vy));
		}

		@Override public String toString(){
			return next!=null ?
					String.format("[%f ~ %f](#%s) -> \n%s", start, end, color, next) :
					String.format("[%f ~ %f](#%s)", start, end, color);
		}
	}

	public static final class Color{
		public static Color of(int red, int green, int blue){
			return new Color(MathHelper.clamp(red, 0, 255)/255.0f,
					MathHelper.clamp(green, 0, 255)/255.0f,
					MathHelper.clamp(blue, 0, 255)/255.0f);
		}
		public static Color of(int red, int green, int blue, int alpha){
			return new Color(MathHelper.clamp(red, 0, 255)/255.0f,
					MathHelper.clamp(green, 0, 255)/255.0f,
					MathHelper.clamp(blue, 0, 255)/255.0f,
					MathHelper.clamp(alpha, 0, 255)/255.0f);
		}

		public float red;
		public float green;
		public float blue;
		public float alpha;

		public Color(float red, float green, float blue){
			this(red, green, blue, 1);
		}
		public Color(float red, float green, float blue, float alpha){
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
		}

		public void set(Color other){
			this.red = other.red;
			this.green = other.green;
			this.blue = other.blue;
			this.alpha = other.alpha;
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

	public enum WheelType{
		FIRST(new ResourceLocation(MODID, "textures/stamina/first.png"), 0, PlayerMovement.BASE_STAMINA),
		SECOND(new ResourceLocation(MODID, "textures/stamina/second.png"), PlayerMovement.BASE_STAMINA, PlayerMovement.BASE_STAMINA*2),
		THIRD(new ResourceLocation(MODID, "textures/stamina/third.png"), PlayerMovement.BASE_STAMINA*2, PlayerMovement.BASE_STAMINA*3);

		public final ResourceLocation texture;
		public final int start;
		public final int end;

		WheelType(ResourceLocation texture, int start, int end){
			this.texture = Objects.requireNonNull(texture);
			this.start = start;
			this.end = end;
		}

		public double getProportion(int value){
			return start>=value ? 0 : end<=value ? 1 : (double)(value-start)/(end-start);
		}
	}
}
