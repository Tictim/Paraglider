package tictim.paraglider.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.client.ParagliderRenderTypes;
import tictim.paraglider.client.render.StaminaWheelConstants;
import tictim.paraglider.client.render.StaminaWheelRenderer.WheelLevel;
import tictim.paraglider.client.render.StaminaWheelState;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.PI;
import static net.minecraft.util.Mth.lerp;
import static tictim.paraglider.client.render.StaminaWheelConstants.*;

@NullMarked
public record StaminaWheelRenderState(
		Matrix3x2f pose,
		TextureSetup textureSetup,
		List<VertexData> vertexData,
		WheelLevel wheelLevel,
		int spriteIndex,
		int radius,
		boolean debug
) implements GuiElementRenderState {
	private static final int WIDTH = 20 * 5;
	private static final int HEIGHT = 20 * 3;

	public static void drawMainWheel(
			GuiGraphicsExtractor grpahics,
			StaminaWheelState wheel,
			boolean debug) {
		if (wheel.alpha() <= 0) return;

		float wheelPos = toWheelPos(wheel.maxStamina());
		int wheels = (int)Math.ceil(wheelPos);

		float maxWheelEnd = wheels > 3 ? 1 : wheelPos - wheels + 1;
		WheelLevel wheelLevel = WheelLevel.mainWheel(wheels - 1);

		submit(grpahics, backgroundVertexData(false, maxWheelEnd, wheel.alpha()), wheelLevel, 0, WHEEL_RADIUS, debug);
		submit(grpahics, backgroundVertexData(true, maxWheelEnd, wheel.alpha()), wheelLevel, 1, WHEEL_RADIUS, debug);

		int l = Math.min(3, wheels);
		for (int i = 0; i < l; i++) {
			submit(grpahics, toVertexData(wheel, switch (i) {
				case 0, 1 -> i;
				default -> Math.max(2, (int)Math.ceil(wheel.staminaWheelPos()) - 1);
			}), wheelLevel, 2 + i, WHEEL_RADIUS, debug);
		}
	}

	public static void drawExtraWheel(
			GuiGraphicsExtractor graphics,
			StaminaWheelState wheel,
			int index,
			boolean debug) {
		if (wheel.alpha() <= 0) return;

		submit(graphics, backgroundVertexData(false, 1, wheel.alpha()), WheelLevel.EXTRA_1, 0, EXTRA_WHEEL_RADIUS, debug);
		submit(graphics, toVertexData(wheel, -1 - index), WheelLevel.EXTRA_1, 1, EXTRA_WHEEL_RADIUS, debug);
	}

	public static void submit(
			GuiGraphicsExtractor graphics,
			List<VertexData> vertexData,
			WheelLevel wheelLevel,
			int spriteIndex,
			int radius,
			boolean debug) {
		if (vertexData.size() < 2) return;

		Matrix3x2f pose = new Matrix3x2f(graphics.pose());
		AbstractTexture t = graphics.minecraft.getTextureManager()
				.getTexture(StaminaWheelConstants.STAMINA_WHEEL_TEXTURE);
		TextureSetup textureSetup = TextureSetup.singleTexture(t.getTextureView(), t.getSampler());

		graphics.submitGuiElementRenderState(new StaminaWheelRenderState(
				pose, textureSetup, vertexData, wheelLevel, spriteIndex, radius, false
		));

		if (debug) {
			graphics.submitGuiElementRenderState(new StaminaWheelRenderState(
					pose, textureSetup, vertexData, wheelLevel, spriteIndex, radius, true
			));
		}
	}

	public static List<VertexData> toVertexData(StaminaWheelState wheel, int wheelIndex) {
		List<VertexData> vertexData = new ArrayList<>();
		float wheelStart = wheelIndex < 0 ? ((float)Math.ceil(toWheelPos(wheel.maxStamina())) + wheelIndex) : wheelIndex;

		var lastColor = 0;
		var edgeIndex = 0;

		for (var e : wheel.segments().float2IntEntrySet()) {
			float point = e.getFloatKey() - wheelStart;
			int color = ARGB.color(wheel.alpha(), e.getIntValue());

			if (point > 0.0) {
				edgeIndex = addEdgeVertices(vertexData, 0, point, edgeIndex, lastColor);

				if (point > 1.0) break;
				if ((lastColor & 0xFF000000) != 0 || (color & 0xFF000000) != 0) {
					vertexData.add(new VertexData(point, lastColor));
				}
			}

			lastColor = color;
		}

		return vertexData;
	}

	public static List<VertexData> backgroundVertexData(boolean emptyArea, float wheelEnd, float alpha) {
		List<VertexData> vertexData = new ArrayList<>();
		int color = ARGB.color(alpha, -1);

		if (!emptyArea) addEdgeVertices(vertexData, 0, wheelEnd, 0, color);
		vertexData.add(new VertexData(wheelEnd, color));
		if (emptyArea) addEdgeVertices(vertexData, wheelEnd, 1, 0, color);

		return vertexData;
	}

	private static int addEdgeVertices(List<VertexData> vertexData, float edgeStart, float point, int edgeIndex, int color) {
		while (edgeIndex < edgePoints.length) {
			float edgePoint = edgePoints[edgeIndex];
			if (edgePoint >= edgeStart) {
				if (edgePoint > point) break;
				vertexData.add(new VertexData(edgePoint, color));
			}
			edgeIndex++;
		}
		return edgeIndex;
	}

	@Override public void buildVertices(VertexConsumer vc) {
		vert(vc, 0, 0, 0.5f, 0.5f, -1);
		for (VertexData d : vertexData) {
			vert(vc, d.point, d.color);
		}
	}

	private static final float[] edgePoints = {0, 1 / 8.0f, 3 / 8.0f, 5 / 8.0f, 7 / 8.0f, 1};

	private void vert(VertexConsumer vc, float point, int color) {
		float vx, vy;
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
		vert(vc, vx * radius, vy * -radius, (float)(vx / 2 + 0.5), (float)(vy / 2 + 0.5), color);
	}

	private void vert(VertexConsumer vc, float x, float y, float u, float v, int color) {
		vc.addVertexWith2DPose(pose, x, y);
		if (!debug) vc.setUv(u(u), v(v));
		vc.setColor(color);
	}

	public float u(float u) {
		float uMin, uMax;

		if (wheelLevel.isExtra()) {
			uMin = WIDTH - 20 + spriteIndex * 10;
			uMax = WIDTH - 10 + spriteIndex * 10;
		} else {
			uMin = spriteIndex * 20;
			uMax = (spriteIndex + 1) * 20;
		}

		return lerp(u * 0.998f + 0.001f, uMin, uMax) / WIDTH;
	}

	public float v(float v) {
		float vMin, vMax;

		if (wheelLevel.isExtra()) {
			vMin = 0;
			vMax = 10;
		} else {
			vMin = wheelLevel.ordinal() * 20;
			vMax = (wheelLevel.ordinal() + 1) * 20;
		}

		return lerp(v * 0.998f + 0.001f, vMax, vMin) / HEIGHT;
	}

	@Override public RenderPipeline pipeline() {
		return this.debug ?
				ParagliderRenderTypes.STAMINA_WHEEL_PIPELINE_DEBUG :
				ParagliderRenderTypes.STAMINA_WHEEL_PIPELINE;
	}

	@Override public TextureSetup textureSetup() {
		return this.textureSetup;
	}

	@Override public @Nullable ScreenRectangle scissorArea() {
		return null;
	}

	@Override public ScreenRectangle bounds() {
		return new ScreenRectangle(-radius, -radius, radius * 2, radius * 2)
				.transformMaxBounds(pose);
	}

	public record VertexData(float point, int color) {}
}
