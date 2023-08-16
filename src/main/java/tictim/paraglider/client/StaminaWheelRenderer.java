package tictim.paraglider.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
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
	public void renderStamina(GuiGraphics guiGraphics, double x, double y, double z){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player==null) return;
		PlayerMovement h = PlayerMovement.of(player);
		if(h==null) return;
		makeWheel(h);

		render(guiGraphics, x, y, z, ModCfg.debugPlayerMovement()&&Paraglider.isParaglider(player.getOffhandItem()));
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

	protected void render(GuiGraphics guiGraphics, double x, double y, double z, boolean debug){
		RenderSystem.disableDepthTest();
		if(debug){
			int linePos = 10;
			Font font = Minecraft.getInstance().font;
			for(WheelLevel t : WheelLevel.values()){
				Wheel wheel = getWheel(t);
				if(wheel!=null){
					linePos = guiGraphics.drawString(font, t+":", 20, linePos, 0xFFFFFFFF, true);
					linePos = guiGraphics.drawString(font, wheel.toString(), 30, linePos, 0xFFFFFFFF, true);
				}
			}
		}

		for(WheelLevel t : WheelLevel.values()){
			Wheel wheel = getWheel(t);
			if(wheel!=null){
				RenderSystem.setShaderTexture(0, t.texture);
				wheel.draw(guiGraphics, x, y, z, WHEEL_RADIUS, debug);
			}
		}

		wheel.clear();
		RenderSystem.setShaderColor(1, 1, 1, 1);
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

		public void draw(GuiGraphics guiGraphics, double x, double y, double z, double radius, boolean debug){
			List<Vec2> debugVertices = debug ? new ArrayList<>() : null;
			drawInternal(x, y, z, radius, debugVertices);

			if(debugVertices!=null){
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(x, y, z);
				Font font = Minecraft.getInstance().font;
				for(Vec2 vec : debugVertices){
					String s = vec.x+" "+vec.y;
					guiGraphics.drawString(font, s,
							(int) (vec.x>0 ? vec.x*(float)radius+2 : vec.x*(float)radius-2-font.width(s)),
							(int) (vec.y>0 ? vec.y*(float)-radius-2-font.lineHeight : vec.y*(float)-radius+2),
							0xFF00FF00, true);
				}
				guiGraphics.pose().popPose();
			}
		}

		private void drawInternal(double x, double y, double z, double radius, @Nullable List<Vec2> debugVertices){
			RenderSystem.setShaderColor(color.red, color.green, color.blue, color.alpha);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder b = tesselator.getBuilder();
			b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
			b.vertex(x, y, z).uv(0.5f, 0.5f).endVertex();
			for(int i = 0; i<renderPoints.length-1; i++){
				double currentStart = renderPoints[i];
				if(currentStart>=end) break;
				double currentEnd = renderPoints[i+1];
				if(currentEnd<=start) continue;

				if(currentStart<=start)
					vert(b, x, y, z, start, radius, debugVertices);
				if(currentEnd>=end) break;
				vert(b, x, y, z, currentEnd, radius, debugVertices);
			}
			vert(b, x, y, z, end, radius, debugVertices);
			tesselator.end();
			if(next!=null) next.drawInternal(x, y, z, radius, debugVertices);
		}

		private void vert(BufferBuilder b, double x, double y, double z, double point, double radius, @Nullable List<Vec2> debugVertices){
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
			b.vertex(x+vx*radius, y+vy*-radius, z).uv((float)(vx/2+0.5), (float)(vy/2+0.5)).endVertex();
			if(debugVertices!=null) debugVertices.add(new Vec2((float)vx, (float)vy));
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
