package tictim.paraglider.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import tictim.paraglider.api.ParagliderAPI;

import static net.minecraft.client.renderer.RenderPipelines.MATRICES_PROJECTION_SNIPPET;

@EventBusSubscriber(modid = ParagliderAPI.MODID, value = Dist.CLIENT)
public final class ParagliderRenderTypes {
	private ParagliderRenderTypes() {}

	public static final RenderPipeline.Snippet STAMINA_WHEEL_PIPELINE_SNIPPET = RenderPipeline.builder(MATRICES_PROJECTION_SNIPPET)
			.withSampler("Sampler0")
			.withCull(false)
			.withBlend(BlendFunction.TRANSLUCENT)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withDepthWrite(false)
			.buildSnippet();

	public static final RenderPipeline STAMINA_WHEEL_PIPELINE = RenderPipeline.builder(STAMINA_WHEEL_PIPELINE_SNIPPET)
			.withLocation(ParagliderAPI.id("pipeline/stamina_wheel"))
			.withVertexShader(ParagliderAPI.id("position_tex_color_flat"))
			.withFragmentShader(ParagliderAPI.id("position_tex_color_flat"))
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
			.build();

	public static final RenderPipeline STAMINA_WHEEL_PIPELINE_DEBUG = RenderPipeline.builder(STAMINA_WHEEL_PIPELINE_SNIPPET)
			.withLocation(ParagliderAPI.id("pipeline/stamina_wheel_debug"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
			.withPolygonMode(PolygonMode.WIREFRAME)
			.build();

	@SubscribeEvent
	public static void register(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(STAMINA_WHEEL_PIPELINE);
		event.registerPipeline(STAMINA_WHEEL_PIPELINE_DEBUG);
	}
}
