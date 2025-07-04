package tictim.paraglider.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.client.settings.ExtraWheelAttachment;
import tictim.paraglider.config.DebugCfg;
import tictim.paraglider.contents.ParagliderTags;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.util.FastColor.ARGB32.alpha;
import static net.minecraft.util.FastColor.ARGB32.color;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

public abstract class StaminaWheelRenderer {
	private static final DecimalFormat DEBUG = new DecimalFormat("#.00");
	private static final Style SMALL_NUMBER_STYLE = Style.EMPTY.withFont(ParagliderAPI.id("small_numbers"));
	private static final int FONT_HEIGHT = 7;

	protected final Wheel mainWheel = new Wheel();
	protected final Wheel extraWheel = new Wheel();

	private boolean debug;
	private @Nullable List<EffectTimer> debugAnims;
	private @Nullable List<String> debugAnimNames;
	private @Nullable FloatList debugVertices; // xy

	/**
	 * Draw stamina wheel with center at (x, y).
	 */
	public void render(@NotNull GuiGraphics guiGraphics, float x, float y, float z, float partialTicks,
	                   @Nullable ExtraWheelAttachment extraWheelAttachment) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		this.debug = isDebugEnabled(player);

		makeWheel(player, partialTicks);
		render(guiGraphics, x, y, z, isDebugEnabled(player), extraWheelAttachment);

		this.mainWheel.reset();
		this.extraWheel.reset();

		if (this.debug) {
			this.debug = false;
			if (this.debugAnims != null) this.debugAnims.clear();
			if (this.debugAnimNames != null) this.debugAnimNames.clear();
			if (this.debugVertices != null) this.debugVertices.clear();
		}
	}

	// by default, holding a paraglider offhand enables stamina wheel debug info
	protected boolean isDebugEnabled(@NotNull Player player) {
		return DebugCfg.get().debugPlayerMovement() && player.getOffhandItem().is(ParagliderTags.PARAGLIDERS);
	}

	protected void debugAnim(String name, EffectTimer anim) {
		if (!this.debug) return;
		if (this.debugAnims == null) this.debugAnims = new ArrayList<>();
		if (this.debugAnimNames == null) this.debugAnimNames = new ArrayList<>();
		this.debugAnims.add(anim);
		this.debugAnimNames.add(name);
	}

	protected abstract void makeWheel(@NotNull Player player, float partialTicks);

	protected void render(@NotNull GuiGraphics guiGraphics,
	                      float x, float y, float z, boolean debug,
	                      @Nullable ExtraWheelAttachment extraWheelAttachment) {
		if (debug) {
			Font font = Minecraft.getInstance().font;
			int lines = 0;

			if (this.debugAnimNames != null && this.debugAnims != null) {
				for (int i = 0; i < this.debugAnimNames.size(); i++) {
					String name = this.debugAnimNames.get(i);
					EffectTimer anim = this.debugAnims.get(i);

					guiGraphics.drawString(font,
							name + ": " + (anim.isActive() ? "active " + anim.activeDuration() : "inactive"),
							20, 10 + font.lineHeight * lines++,
							0xFFFFFFFF);
				}
			}

			if (lines > 0) lines++;

			int lineStart = lines;
			int maxWidth = 0;

			for (int i = 0; i < this.mainWheel.count; i++) {
				Wheel.Segment s = this.mainWheel.segments.get(i);
				String segmentString = DEBUG.format(s.from) + " ~ " + DEBUG.format(s.to) + ": ";

				guiGraphics.drawString(font, segmentString,
						20, 10 + font.lineHeight * lines++,
						0xFFFFFFFF);
				maxWidth = Math.max(maxWidth, font.width(segmentString));
			}

			lines = lineStart;

			for (int i = 0; i < this.mainWheel.count; i++) {
				Wheel.Segment s = this.mainWheel.segments.get(i);
				guiGraphics.drawString(font, String.format("#%X", s.color),
						20 + maxWidth, 10 + font.lineHeight * lines++,
						color(Math.max(255, alpha(s.color) * 2), s.color));
			}
		}

		PoseStack pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(x, y, z);

		drawWheels(guiGraphics, extraWheelAttachment);

		if (this.debugVertices != null) {
			Font font = Minecraft.getInstance().font;
			for (int i = 0; i < this.debugVertices.size(); i += 2) {
				float vx = this.debugVertices.getFloat(i);
				float vy = this.debugVertices.getFloat(i + 1);

				String s = DEBUG.format(vx) + " " + DEBUG.format(vy);
				Vector2f v2 = new Vector2f(vx, vy).normalize().mul(WHEEL_RADIUS * 2);

				guiGraphics.drawString(font, s,
						(int)(vx > 0 ? v2.x : v2.x - font.width(s)),
						(int)(vy > 0 ? -v2.y - font.lineHeight : -v2.y),
						wheelColor(0));
			}
		}

		pose.popPose();
	}

	private static final float[] edgePoints = {0, 1 / 8.0f, 3 / 8.0f, 5 / 8.0f, 7 / 8.0f, 1};

	protected void drawWheels(@NotNull GuiGraphics guiGraphics, @Nullable ExtraWheelAttachment extraWheelAttachment) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		drawWheel(guiGraphics, this.mainWheel, WheelLevel.FIRST, WHEEL_RADIUS);
		drawWheel(guiGraphics, this.mainWheel, WheelLevel.SECOND, WHEEL_RADIUS);
		drawWheel(guiGraphics, this.mainWheel, WheelLevel.THIRD, WHEEL_RADIUS);

		guiGraphics.flush();

		if (this.extraWheel.stamina() > 0 && extraWheelAttachment != null) {
			PoseStack pose = guiGraphics.pose();

			int wheels = Math.min(3, (int)Math.ceil(toWheelPos(this.mainWheel.maxStamina)) - 1);
			boolean hasTwoExtraWheels = this.extraWheel.stamina > Stamina.STAMINA_PER_WHEEL;

			pose.pushPose();
			pose.translate(
					extraWheelOffsetX(extraWheelAttachment, wheels, hasTwoExtraWheels, 0),
					extraWheelOffsetY(extraWheelAttachment, wheels, hasTwoExtraWheels, 0),
					0);
			drawWheel(guiGraphics, this.extraWheel, WheelLevel.EXTRA_1, EXTRA_WHEEL_RADIUS);
			pose.popPose();

			if (hasTwoExtraWheels) {
				pose.pushPose();
				pose.translate(
						extraWheelOffsetX(extraWheelAttachment, wheels, true, 1),
						extraWheelOffsetY(extraWheelAttachment, wheels, true, 1),
						0);
				drawWheel(guiGraphics, this.extraWheel, WheelLevel.EXTRA_2, EXTRA_WHEEL_RADIUS);
				pose.popPose();
			}
		}

		int color = this.mainWheel.indicatorColor();
		if (alpha(color) >= 4) {
			drawText(guiGraphics, "+" + Math.max(1, (int)(Math.ceil(this.mainWheel.staminaWheelPos()) - 3)),
					false, WHEEL_RADIUS - 1, 1 - WHEEL_RADIUS, color);
		}

		if (this.extraWheel.stamina() > Stamina.STAMINA_PER_WHEEL * 2 && extraWheelAttachment != null) {
			color = this.extraWheel.indicatorColor();
			if (alpha(color) >= 4) {
				drawText(guiGraphics, "+" + Math.max(1, (int)(Math.ceil(toWheelPos(this.extraWheel.stamina())) - 2)),
						true, 1 - WHEEL_RADIUS, 1 - WHEEL_RADIUS, color);
			}
		}

		RenderSystem.disableBlend();
	}

	protected double extraWheelOffsetX(ExtraWheelAttachment extraWheelAttachment, int wheels, boolean hasTwoExtraWheels, int index) {
		final double extraWheelMargin = EXTRA_WHEEL_RADIUS * 2 + 2.5;

		return switch (extraWheelAttachment) {
			case TOP, BOTTOM -> {
				if (!hasTwoExtraWheels) yield 0.0;
				else yield (index == 0 ? .5 : -.5) * extraWheelMargin;
			}
			case LEFT, RIGHT -> {
				double offset = -WHEEL_RADIUS - EXTRA_WHEEL_RADIUS - wheels - 0.5;
				if (index == 1) offset -= extraWheelMargin;
				if (extraWheelAttachment == ExtraWheelAttachment.RIGHT) offset = -offset;
				yield offset;
			}
		};
	}

	protected double extraWheelOffsetY(ExtraWheelAttachment extraWheelAttachment, int wheels, boolean hasTwoExtraWheels, int index) {
		return switch (extraWheelAttachment) {
			case LEFT, RIGHT -> 0;
			case TOP, BOTTOM -> {
				double offset = -WHEEL_RADIUS - EXTRA_WHEEL_RADIUS - wheels - 0.5;
				if (hasTwoExtraWheels) offset += 2;
				if (extraWheelAttachment == ExtraWheelAttachment.BOTTOM) offset = -offset;
				yield offset;
			}
		};
	}

	protected void drawText(GuiGraphics guiGraphics, String text, boolean alignRight,
	                        int x, int y, int color) {
		Font font = Minecraft.getInstance().font;
		PoseStack pose = guiGraphics.pose();

		pose.pushPose();
		pose.translate(x, y, 0);
		pose.scale(.5f, .5f, 1);

		MutableComponent component = Component.literal(text).setStyle(SMALL_NUMBER_STYLE);
		guiGraphics.drawString(font, component,
				alignRight ? -font.width(component) : 0, -FONT_HEIGHT / 2, color);

		pose.popPose();
	}

	protected void drawWheel(GuiGraphics guiGraphics, Wheel wheel, WheelLevel wheelLevel, float radius) {
		float wheelStart = switch (wheelLevel) {
			case FIRST -> 0;
			case SECOND -> 1;
			case THIRD -> Math.max(2, (float)Math.ceil(wheel.staminaWheelPos()) - 1);
			case EXTRA_1 -> (float)Math.ceil(wheel.staminaWheelPos()) - 1;
			case EXTRA_2 -> (float)Math.ceil(wheel.staminaWheelPos()) - 2;
		};

		for (int i = 0; i < wheel.count; i++) {
			Wheel.Segment segment = wheel.segments.get(i);

			int alpha = alpha(segment.color);
			if (alpha <= 0) continue;

			float start = segment.from - wheelStart;
			if (start >= 1) continue;

			float end = segment.to - wheelStart;
			if (end <= 0) continue;

			drawSegment(guiGraphics, wheelLevel, start, end, segment.color, radius);
		}
	}

	protected void drawSegment(GuiGraphics guiGraphics, WheelLevel wheelLevel,
	                           float start, float end, int color, float radius) {
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, wheelLevel.texture);

		BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);
		b.addVertex(guiGraphics.pose().last(), 0, 0, 0).setUv(0.5f, 0.5f).setColor(color);

		int edgeIndex = 0;
		int segmentIndex = 0;

		while (edgeIndex < edgePoints.length && segmentIndex < 2) {
			float currentSegment = segmentIndex == 0 ? start : end;
			float currentEdge = edgePoints[edgeIndex];

			if (currentSegment <= currentEdge) {
				if (currentSegment > 0) {
					vert(guiGraphics, b, currentSegment, radius, color);
				}
				segmentIndex++;
			} else {
				if (segmentIndex > 0) {
					vert(guiGraphics, b, currentEdge, radius, color);
				}
				edgeIndex++;
			}
		}

		BufferUploader.drawWithShader(b.buildOrThrow());
	}

	protected void vert(GuiGraphics guiGraphics, VertexConsumer vc, float point, float radius, int color) {
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
		vc.addVertex(guiGraphics.pose().last(), vx * radius, vy * -radius, 0)
				.setUv((float)(vx / 2 + 0.5), (float)(vy / 2 + 0.5))
				.setColor(color);
		if (this.debug) {
			if (this.debugVertices == null) this.debugVertices = new FloatArrayList();
			this.debugVertices.add(vx);
			this.debugVertices.add(vy);
		}
	}

	public static final class Wheel {
		private final List<Segment> segments = new ArrayList<>();
		private double stamina;
		private double maxStamina;
		private int count;

		private int indicatorColor;

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

		public void setIndicatorColor(int indicatorColor) {
			this.indicatorColor = indicatorColor;
		}

		public float staminaWheelPos() {
			return toWheelPos((int)Math.min(maxStamina(), stamina()));
		}

		public void fillStamina(double from, double to, int color) {
			fillWheel(toWheelPos(Math.clamp(from, 0, this.maxStamina)),
					toWheelPos(Math.clamp(to, 0, this.maxStamina)), color);
		}

		public void fillWheel(float from, float to, int color) {
			from = Math.max(0, from);
			if (from < to) {
				insert(searchForBaseSegment(from), from, to, color);
			}
		}

		private int searchForBaseSegment(float from) {
			int l = 0, r = this.count;
			while (l < r) {
				int m = l + (r - l) / 2;
				Segment s = this.segments.get(m);
				if (s.from < from) l = m + 1;
				else r = m;
			}
			return l;
		}

		private void insert(int index, float from, float to, int color) {
			for (int i = index; i < this.count; i++) {
				Segment s = this.segments.get(i);
				if (s.from < from) {
					index++;
					s.to = from;
				} else if (s.to <= to) {
					removeSegment(i--);
				} else {
					s.from = to;
					break;
				}
			}

			int lastIndex = this.segments.size() - 1;
			Segment s = lastIndex > this.count ? this.segments.remove(lastIndex) : new Segment();
			s.from = from;
			s.to = to;
			s.color = color;
			this.segments.add(index, s);
			this.count++;
		}

		private void removeSegment(int index) {
			this.segments.add(this.segments.remove(index));
			this.count--;
		}

		public void reset() {
			this.count = 0;
			this.maxStamina = 0;
			this.indicatorColor = 0;
		}

		private static final class Segment {
			private float from;
			private float to;
			private int color; // ARGB

			@Override public String toString() {
				return String.format("%s ~ %s: #%X", DEBUG.format(from), DEBUG.format(to), color);
			}
		}
	}

	public enum WheelLevel {
		FIRST(ParagliderAPI.id("textures/stamina/first.png")),
		SECOND(ParagliderAPI.id("textures/stamina/second.png")),
		THIRD(ParagliderAPI.id("textures/stamina/third.png")),
		EXTRA_1(ParagliderAPI.id("textures/stamina/extra.png")),
		EXTRA_2(EXTRA_1.texture);

		public final ResourceLocation texture;

		WheelLevel(ResourceLocation texture) {
			this.texture = Objects.requireNonNull(texture);
		}
	}
}
