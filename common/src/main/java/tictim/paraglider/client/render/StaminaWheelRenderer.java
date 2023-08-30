package tictim.paraglider.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.item.Paraglider;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.DebugCfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.PI;
import static net.minecraft.util.FastColor.ARGB32.*;
import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public abstract class StaminaWheelRenderer{
	private final Wheel wheel = new Wheel();

	/**
	 * Draw stamina wheel with center at (x, y).
	 */
	public void renderStamina(@NotNull GuiGraphics graphics, double x, double y, double z){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player==null) return;
		makeWheel(player, wheel);
		render(graphics, wheel, x, y, z, isDebugEnabled(player));
		wheel.reset();
	}

	// by default, holding a paraglider offhand enables stamina wheel debug info
	protected boolean isDebugEnabled(@NotNull Player player){
		return DebugCfg.get().debugPlayerMovement()&&player.getOffhandItem().getItem() instanceof Paraglider;
	}

	protected abstract void makeWheel(@NotNull Player player, @NotNull Wheel wheel);

	protected void render(@NotNull GuiGraphics graphics, @NotNull Wheel wheel, double x, double y, double z, boolean debug){
		RenderSystem.disableDepthTest();
		if(debug){
			Font font = Minecraft.getInstance().font;
			for(int i = 0; i<wheel.count; i++){
				graphics.drawString(font, wheel.entries.get(i).toString(), 20, 10+font.lineHeight*i, 0xFFFFFFFF);
			}
		}

		draw(graphics, wheel, x, y, z, WHEEL_RADIUS, debug);
	}

	private static final double[] renderPoints = {0, 1/8.0, 3/8.0, 5/8.0, 7/8.0, 1};

	private static void draw(@NotNull GuiGraphics graphics, @NotNull Wheel wheel, double x, double y, double z, double radius, boolean debug){
		List<Vec2> debugVertices = debug ? new ArrayList<>() : null;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		for(WheelLevel wheelLevel : WheelLevel.values()){
			for(int i = 0; i<wheel.count; i++){
				Wheel.Entry entry = wheel.entries.get(i);
				int alpha = alpha(entry.color);
				if(alpha<=0) continue;
				double start = wheelLevel.getProportion(entry.from);
				if(start>=1) continue;
				double end = wheelLevel.getProportion(entry.to);
				if(end<=0) continue;

				RenderSystem.setShaderColor(
						red(entry.color)/(float)0xFF,
						green(entry.color)/(float)0xFF,
						blue(entry.color)/(float)0xFF,
						alpha/(float)0xFF);

				RenderSystem.setShaderTexture(0, wheelLevel.texture);
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder b = tesselator.getBuilder();
				b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
				b.vertex(x, y, z).uv(0.5f, 0.5f).endVertex();

				for(int j = 0; j<renderPoints.length-1; j++){
					double currentStart = renderPoints[j];
					if(currentStart>=end) break;
					double currentEnd = renderPoints[j+1];
					if(currentEnd<=start) continue;

					if(currentStart<=start)
						vert(b, x, y, z, start, radius, debugVertices);
					if(currentEnd>=end) break;
					vert(b, x, y, z, currentEnd, radius, debugVertices);
				}
				vert(b, x, y, z, end, radius, debugVertices);
				tesselator.end();
			}
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);

		if(debugVertices!=null){
			PoseStack stack = graphics.pose();
			stack.pushPose();
			stack.translate(x, y, z);
			Font font = Minecraft.getInstance().font;
			for(Vec2 vec : debugVertices){
				String s = vec.x+" "+vec.y;
				graphics.drawString(font, s,
						(int)(vec.x>0 ? vec.x*(float)radius+2 : vec.x*(float)radius-2-font.width(s)),
						(int)(vec.y>0 ? vec.y*(float)-radius-2-font.lineHeight : vec.y*(float)-radius+2),
						0xFF00FF00);
			}
			stack.popPose();
		}
	}

	private static void vert(BufferBuilder b, double x, double y, double z, double point, double radius, @Nullable List<Vec2> debugVertices){
		double vx, vy;
		// @formatter:off
		if(point==0||point==1){
			vx = 0; vy = 1;
		}else if(point==1/8.0){
			vx = -1; vy = 1;
		}else if(point==3/8.0){
			vx = -1; vy = -1;
		}else if(point==5/8.0){
			vx = 1; vy = -1;
		}else if(point==7/8.0){
			vx = 1; vy = 1;
		}else if(point<1/8.0||point>7/8.0){
			vx = -Math.tan(point*(2*PI)); vy = 1;
		}else if(point<3/8.0){
			vx = -1; vy = 1/Math.tan(point*(2*PI));
		}else if(point<5/8.0){
			vx = Math.tan(point*(2*PI)); vy = -1;
		}else{
			vx = 1; vy = -1/Math.tan(point*(2*PI));
		}
		// @formatter:on
		b.vertex(x+vx*radius, y+vy*-radius, z).uv((float)(vx/2+0.5), (float)(vy/2+0.5)).endVertex();
		if(debugVertices!=null) debugVertices.add(new Vec2((float)vx, (float)vy));
	}

	public static final class Wheel{
		private final List<Entry> entries = new ArrayList<>();
		private int count;

		public void fill(int from, int to, int color){
			if(from>=to) return;

			for(int i = 0; i<count; i++){
				Entry e = entries.get(i);
				if(e.to<=from) continue;
				insert(e.from<from ? i+1 : i, from, to, color);
				return;
			}
			// no entries, just add
			insert(count, from, to, color);
		}

		private void insert(int index, int from, int to, int color){
			int lastIndex = entries.size()-1;
			Entry entry = lastIndex>count ? entries.remove(lastIndex) : new Entry();
			entries.add(index, entry);
			count++;
			entry.from = from;
			entry.to = to;
			entry.color = color;

			for(int i = index+1; i<count; i++){
				Entry e = entries.get(i);
				if(e.to<=to){
					remove(i--);
				}else{
					e.from = to;
					return;
				}
			}
		}

		private void remove(int index){
			entries.add(entries.remove(index));
			count--;
		}

		public void reset(){
			this.count = 0;
		}

		private static final class Entry{
			private int from;
			private int to;
			private int color; // ARGB

			@Override public String toString(){
				return String.format("%d ~ %d: #%X", from, to, color);
			}
		}
	}

	public enum WheelLevel{
		FIRST(ParagliderAPI.id("textures/stamina/first.png")),
		SECOND(ParagliderAPI.id("textures/stamina/second.png")),
		THIRD(ParagliderAPI.id("textures/stamina/third.png"));

		public final ResourceLocation texture;

		WheelLevel(ResourceLocation texture){
			this.texture = Objects.requireNonNull(texture);
		}

		public int start(){
			return switch(this){
				case FIRST -> 0;
				case SECOND -> (int)(Cfg.get().maxStamina()/3.0);
				case THIRD -> (int)(Cfg.get().maxStamina()*2/3.0);
			};
		}
		public int end(){
			return switch(this){
				case FIRST -> (int)(Cfg.get().maxStamina()/3.0);
				case SECOND -> (int)(Cfg.get().maxStamina()*2/3.0);
				case THIRD -> Cfg.get().maxStamina();
			};
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