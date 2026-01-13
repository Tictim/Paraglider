package tictim.paraglider.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.Float2IntSortedMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import tictim.paraglider.client.ParagliderRenderTypes;
import tictim.paraglider.client.render.StaminaWheelRenderer;
import tictim.paraglider.client.render.StaminaWheelState;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.PI;

public record StaminaWheelRenderState(
		Matrix3x2f pose,
		TextureSetup textureSetup,
		List<VertexData> vertexData,
		int radius,
		boolean debug
) implements GuiElementRenderState {
	public static void submit(
			GuiGraphics guiGraphics,
			StaminaWheelState wheel,
			StaminaWheelRenderer.WheelLevel wheelLevel,
			boolean debug) {
		var vertexData = toVertexData(wheel, wheelLevel);
		if (vertexData.isEmpty()) return;

		Matrix3x2f pose = new Matrix3x2f(guiGraphics.pose());
		AbstractTexture t = guiGraphics.minecraft.getTextureManager().getTexture(wheelLevel.texture);
		TextureSetup textureSetup = TextureSetup.singleTexture(t.getTextureView(), t.getSampler());

		guiGraphics.submitGuiElementRenderState(new StaminaWheelRenderState(
				pose, textureSetup, vertexData, wheelLevel.radius, false
		));

		if (debug) {
			guiGraphics.submitGuiElementRenderState(new StaminaWheelRenderState(
					pose, textureSetup, vertexData, wheelLevel.radius, true
			));
		}
	}

	public static List<VertexData> toVertexData(StaminaWheelState wheel, StaminaWheelRenderer.WheelLevel wheelLevel) {
		List<VertexData> vertexData = new ArrayList<>();

		float wheelStart = switch (wheelLevel) {
			case FIRST -> 0;
			case SECOND -> 1;
			case THIRD -> Math.max(2, (float) Math.ceil(wheel.staminaWheelPos()) - 1);
			case EXTRA_1 -> (float) Math.ceil(wheel.staminaWheelPos()) - 1;
			case EXTRA_2 -> (float) Math.ceil(wheel.staminaWheelPos()) - 2;
		};

		var lastColor = 0;
		var edgeIndex = 0;

		Float2IntSortedMap headMap = wheel.segments().headMap(wheelStart + 1);
		for (var e : headMap.float2IntEntrySet()) {
			float point = e.getFloatKey() - wheelStart;
			int color = e.getIntValue();

			if (point > 0.0) {
				while (edgeIndex < edgePoints.length) {
					float edgePoint = edgePoints[edgeIndex];
					if (edgePoint > point) break;
					vertexData.add(new VertexData(edgePoint, lastColor));
					edgeIndex++;
				}

				if ((lastColor & 0xFF000000) != 0 || (color & 0xFF000000) != 0) {
					vertexData.add(new VertexData(point, lastColor));
				}
			}

			lastColor = color;
		}

		if ((lastColor & 0xFF000000) != 0) {
			while (edgeIndex < edgePoints.length) {
				vertexData.add(new VertexData(edgePoints[edgeIndex++], lastColor));
			}
		}

		return vertexData;
	}

	@Override public void buildVertices(@NotNull VertexConsumer vc) {
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
			vx = (float) -Math.tan(point * (2 * PI));
			vy = 1;
		} else if (point < 3 / 8.0) {
			vx = -1;
			vy = 1 / (float) Math.tan(point * (2 * PI));
		} else if (point < 5 / 8.0) {
			vx = (float) Math.tan(point * (2 * PI));
			vy = -1;
		} else {
			vx = 1;
			vy = -1 / (float) Math.tan(point * (2 * PI));
		}
		vert(vc, vx * radius, vy * -radius, (float) (vx / 2 + 0.5), (float) (vy / 2 + 0.5), color);
	}

	private void vert(VertexConsumer vc, float x, float y, float u, float v, int color) {
		vc.addVertexWith2DPose(pose, x, y);
		if (!debug) vc.setUv(u, v);
		vc.setColor(color);
	}

	@Override public @NotNull RenderPipeline pipeline() {
		return this.debug ?
				ParagliderRenderTypes.STAMINA_WHEEL_PIPELINE_DEBUG :
				ParagliderRenderTypes.STAMINA_WHEEL_PIPELINE;
	}

	@Override public @NotNull TextureSetup textureSetup() {
		return this.textureSetup;
	}

	@Override public @Nullable ScreenRectangle scissorArea() {
		return null;
	}

	@Override public @NotNull ScreenRectangle bounds() {
		return new ScreenRectangle(-radius, -radius, radius * 2, radius * 2)
				.transformMaxBounds(pose);
	}

	public record VertexData(float point, int color) {}
}
