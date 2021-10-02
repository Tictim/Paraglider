package tictim.paraglider.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.opengl.GL11;
import tictim.paraglider.ModCfg;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.utils.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.PI;
import static tictim.paraglider.ParagliderMod.MODID;
import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

public abstract class StaminaWheelRenderer{
	private final Map<WheelLevel, Wheel> wheel = new EnumMap<>(WheelLevel.class);

	/**
	 * Draw stamina wheel with center at (x, y).
	 */
	public void renderStamina(MatrixStack matrixStack, double x, double y, double z){
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if(player==null) return;
		PlayerMovement h = PlayerMovement.of(player);
		if(h==null) return;
		makeWheel(h);

		render(matrixStack, x, y, z, ModCfg.debugPlayerMovement()&&Paraglider.isParaglider(player.getHeldItemOffhand()));
	}

	protected abstract void makeWheel(PlayerMovement h);

	@Nullable protected Wheel getWheel(WheelLevel type){
		return wheel.get(type);
	}

	protected void addWheel(WheelLevel wheelLevel, double start, double end, Color color){
		start = Math.max(0, start);
		end = Math.min(1, end);
		if(start>=end) return;

		Wheel wheel = this.wheel.get(wheelLevel);
		this.wheel.put(wheelLevel, wheel!=null ? wheel.insert(new Wheel(start, end, color)) : new Wheel(start, end, color));
	}

	protected void render(MatrixStack stack, double x, double y, double z, boolean debug){
		RenderSystem.disableDepthTest();

		BufferBuilder b = Tessellator.getInstance().getBuffer();
		Minecraft mc = Minecraft.getInstance();

		if(debug){
			float linePos = 10;
			FontRenderer font = mc.fontRenderer;
			for(WheelLevel t : WheelLevel.values()){
				Wheel wheel = getWheel(t);
				if(wheel!=null){
					linePos = font.drawStringWithShadow(stack, t+":", 20, linePos, 0xFFFFFFFF);
					linePos = font.drawStringWithShadow(stack, wheel.toString(), 30, linePos, 0xFFFFFFFF);
				}
			}
		}

		for(WheelLevel t : WheelLevel.values()){
			Wheel wheel = getWheel(t);
			if(wheel!=null){
				mc.getTextureManager().bindTexture(t.texture);
				//noinspection deprecation
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				wheel.draw(stack, b, x, y, z, WHEEL_RADIUS, debug);
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

		public void draw(MatrixStack stack, BufferBuilder b, double x, double y, double z, double radius, boolean debug){
			List<Vector2f> debugVertices = debug ? new ArrayList<>() : null;
			b.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR_TEX);
			b.pos(x, y, z).color(color.red, color.green, color.blue, color.alpha).tex(0.5f, 0.5f).endVertex();
			drawInternal(b, x, y, z, radius, debugVertices, false);
			b.finishDrawing();
			WorldVertexBufferUploader.draw(b);

			if(debugVertices!=null){
				stack.push();
				stack.translate(x, y, z);
				FontRenderer font = Minecraft.getInstance().fontRenderer;
				for(Vector2f vec : debugVertices){
					String s = vec.x+" "+vec.y;
					font.drawStringWithShadow(stack, s,
							vec.x>0 ? vec.x*(float)radius+2 : vec.x*(float)radius-2-font.getStringWidth(s),
							vec.y>0 ? vec.y*(float)-radius-2-font.FONT_HEIGHT : vec.y*(float)-radius+2,
							0xFF00FF00);
				}
				stack.pop();
			}
		}
		private void drawInternal(BufferBuilder b, double x, double y, double z, double radius, @Nullable List<Vector2f> debugVertices, boolean skipFirst){
			for(int i = 0; i<renderPoints.length-1; i++){
				double currentStart = renderPoints[i];
				if(currentStart>=end) break;
				double currentEnd = renderPoints[i+1];
				if(currentEnd<=start) continue;

				if(currentStart<=start){
					if(!skipFirst) vert(b, x, y, z, start, radius, debugVertices);
				}
				vert(b, x, y, z, Math.min(currentEnd, end), radius, debugVertices);
			}
			if(next!=null) next.drawInternal(b, x, y, z, radius, debugVertices, end==next.start);
		}

		@SuppressWarnings("ConstantConditions")
		private void vert(BufferBuilder b, double x, double y, double z, double point, double radius, @Nullable List<Vector2f> debugVertices){
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
			b.pos(x+vx*radius, y+vy*-radius, z).color(color.red, color.green, color.blue, color.alpha).tex((float)(vx/2+0.5), (float)(vy/2+0.5)).endVertex();
			if(debugVertices!=null) debugVertices.add(new Vector2f((float)vx, (float)vy));
		}

		@Override public String toString(){
			return next!=null ?
					String.format("[%f ~ %f](#%s) -> \n%s", start, end, color, next) :
					String.format("[%f ~ %f](#%s)", start, end, color);
		}
	}

	public enum WheelLevel{
		FIRST(new ResourceLocation(MODID, "textures/stamina/first.png")),
		SECOND(new ResourceLocation(MODID, "textures/stamina/second.png")),
		THIRD(new ResourceLocation(MODID, "textures/stamina/third.png"));

		public final ResourceLocation texture;

		WheelLevel(ResourceLocation texture){
			this.texture = Objects.requireNonNull(texture);
		}

		private int start(){
			return ModCfg.startingStamina()*ordinal();
		}
		private int end(){
			return ModCfg.startingStamina()*(1+ordinal());
		}

		public double getProportion(int value){
			int start = start();
			if(start>=value) return 0;
			int end = end();
			if(end<=value) return 1;
			return (double)(value-start)/(end-start);
		}
	}
}
