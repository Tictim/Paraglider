package tictim.paraglider.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.ParagliderRenderTypes;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.contents.ParagliderTags;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.util.ARGB.alpha;
import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public abstract class StaminaWheelRenderer {
	private static final DecimalFormat DEBUG = new DecimalFormat("#.00");

	private final Wheel wheel = new Wheel();

	/**
	 * Draw stamina wheel with center at (x, y).
	 */
	public void render(@NotNull GuiGraphics guiGraphics, float x, float y, float z) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		makeWheel(player, this.wheel);
		render(guiGraphics, this.wheel, x, y, z, isDebugEnabled(player));
		this.wheel.reset();
	}

	// by default, holding a paraglider offhand enables stamina wheel debug info
	protected boolean isDebugEnabled(@NotNull Player player) {
		return DebugCfg.get().debugPlayerMovement() && player.getOffhandItem().is(ParagliderTags.PARAGLIDERS);
	}

	protected abstract void makeWheel(@NotNull Player player, @NotNull Wheel wheel);

	protected void render(@NotNull GuiGraphics guiGraphics, @NotNull Wheel wheel, float x, float y, float z, boolean debug) {
		if (debug) {
			Font font = Minecraft.getInstance().font;
			for (int i = 0; i < wheel.count; i++) {
				guiGraphics.drawString(font, wheel.segments.get(i).toString(),
						20, 10 + font.lineHeight * i,
						0xFFFFFFFF);
			}
		}

		draw(guiGraphics, wheel, x, y, z, WHEEL_RADIUS, debug);
	}

	private static final float[] renderPoints = {0, 1 / 8.0f, 3 / 8.0f, 5 / 8.0f, 7 / 8.0f, 1};

	private static void draw(GuiGraphics guiGraphics, Wheel wheel,
	                         float x, float y, float z, float radius, boolean debug) {
		List<Vec2> debugVertices = debug ? new ArrayList<>() : null;

		guiGraphics.drawSpecial(s -> {
			for (WheelLevel wheelLevel : WheelLevel.values()) {
				for (int i = 0; i < wheel.count; i++) {
					Wheel.Segment segment = wheel.segments.get(i);
					int alpha = alpha(segment.color);
					if (alpha <= 0) continue;
					float start = wheelLevel.getProportion(segment.from);
					if (start >= 1) continue;
					float end = wheelLevel.getProportion(segment.to);
					if (end <= 0) continue;

					VertexConsumer b = s.getBuffer(ParagliderRenderTypes.STAMINA_WHEEL.apply(wheelLevel.texture));
					b.addVertex(x, y, z).setUv(0.5f, 0.5f).setColor(segment.color);

					for (int j = 0; j < renderPoints.length - 1; j++) {
						float currentStart = renderPoints[j];
						if (currentStart >= end) break;
						float currentEnd = renderPoints[j + 1];
						if (currentEnd <= start) continue;

						if (currentStart <= start)
							vert(b, x, y, z, start, radius, segment.color, debugVertices);
						if (currentEnd >= end) break;
						vert(b, x, y, z, currentEnd, radius, segment.color, debugVertices);
					}
					vert(b, x, y, z, end, radius, segment.color, debugVertices);
				}
			}
		});

		if (debugVertices != null) {
			PoseStack stack = guiGraphics.pose();
			stack.pushPose();
			stack.translate(x, y, z);
			Font font = Minecraft.getInstance().font;
			for (Vec2 vec : debugVertices) {
				String s = DEBUG.format(vec.x) + " " + DEBUG.format(vec.y);
				Vec2 v2 = vec.normalized().scale(radius * 2);
				guiGraphics.drawString(font, s,
						(int)(vec.x > 0 ? v2.x : v2.x - font.width(s)),
						(int)(vec.y > 0 ? -v2.y - font.lineHeight : -v2.y),
						0xFF00FF00);
			}
			stack.popPose();
		}
	}

	private static void vert(VertexConsumer b, float x, float y, float z, float point, float radius, int color,
	                         @Nullable List<Vec2> debugVertices) {
		float vx, vy;
		final float PI = Mth.PI;
		if (point == 0 || point == 1) {
			vx = 0;
			vy = 1;
		} else if (point == 1 / 8.0) {
			vx = -1;
			vy = 1;
		} else if (point == 3 / 8.0) {
			vx = -1;
			vy = -1;
		} else if (point == 5 / 8.0) {
			vx = 1;
			vy = -1;
		} else if (point == 7 / 8.0) {
			vx = 1;
			vy = 1;
		} else if (point < 1 / 8.0 || point > 7 / 8.0) {
			vx = (float)-Math.tan(point * (2 * PI));
			vy = 1;
		} else if (point < 3 / 8.0) {
			vx = -1;
			vy = 1 / (float)Math.tan(point * (2 * PI));
		} else if (point < 5 / 8.0) {
			vx = (float)Math.tan(point * (2 * PI));
			vy = -1;
		} else {
			vx = 1;
			vy = -1 / (float)Math.tan(point * (2 * PI));
		}
		b.addVertex(x + vx * radius, y + vy * -radius, z)
				.setUv((float)(vx / 2 + 0.5), (float)(vy / 2 + 0.5))
				.setColor(color);
		if (debugVertices != null) debugVertices.add(new Vec2(vx, vy));
	}

	public static final class Wheel {
		private final List<Segment> segments = new ArrayList<>();
		private int count;

		public void fill(int from, int to, int color) {
			if (from >= to) return;

			for (int i = 0; i < this.count; i++) {
				Segment s = this.segments.get(i);
				if (s.to <= from) continue;
				insert(s.from < from ? i + 1 : i, from, to, color);
				return;
			}
			// no entries, just add
			insert(this.count, from, to, color);
		}

		private void insert(int index, int from, int to, int color) {
			int lastIndex = this.segments.size() - 1;
			Segment s = lastIndex > this.count ? this.segments.remove(lastIndex) : new Segment();
			this.segments.add(index, s);
			this.count++;
			s.from = from;
			s.to = to;
			s.color = color;

			for (int i = index + 1; i < this.count; i++) {
				Segment e = segments.get(i);
				if (e.to <= to) {
					remove(i--);
				} else {
					e.from = to;
					return;
				}
			}
		}

		private void remove(int index) {
			this.segments.add(this.segments.remove(index));
			this.count--;
		}

		public void reset() {
			this.count = 0;
		}

		private static final class Segment {
			private int from;
			private int to;
			private int color; // ARGB

			@Override public String toString() {
				return String.format("%d ~ %d: #%X", from, to, color);
			}
		}
	}

	public enum WheelLevel {
		FIRST(ParagliderAPI.id("textures/stamina/first.png")),
		SECOND(ParagliderAPI.id("textures/stamina/second.png")),
		THIRD(ParagliderAPI.id("textures/stamina/third.png"));

		public final ResourceLocation texture;

		WheelLevel(ResourceLocation texture) {
			this.texture = Objects.requireNonNull(texture);
		}

		public int start() {
			return switch (this) {
				case FIRST -> 0;
				case SECOND -> (int)(Cfg.get().maxStamina() / 3.0);
				case THIRD -> (int)(Cfg.get().maxStamina() * 2 / 3.0);
			};
		}
		public int end() {
			return switch (this) {
				case FIRST -> (int)(Cfg.get().maxStamina() / 3.0);
				case SECOND -> (int)(Cfg.get().maxStamina() * 2 / 3.0);
				case THIRD -> Cfg.get().maxStamina();
			};
		}

		public float getProportion(int value) {
			int start = start();
			if (start >= value) return 0;
			int end = end();
			if (end <= value) return 1;
			return (float)(value - start) / (end - start);
		}
	}
}