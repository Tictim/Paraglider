package tictim.paraglider.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import tictim.paraglider.api.ParagliderAPI;

import java.util.Optional;

import static net.minecraft.client.renderer.RenderPipelines.GLOBALS_SNIPPET;

@EventBusSubscriber(modid = ParagliderAPI.MODID, value = Dist.CLIENT)
public final class ParagliderRenderTypes {
	private ParagliderRenderTypes() {}

	public static final RenderPipeline.Snippet STAMINA_WHEEL_PIPELINE_SNIPPET = RenderPipeline.builder(GLOBALS_SNIPPET)
			.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withCull(false)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(Optional.empty())
			.buildSnippet();

	public static final RenderPipeline STAMINA_WHEEL_PIPELINE = RenderPipeline.builder(STAMINA_WHEEL_PIPELINE_SNIPPET)
			.withLocation(ParagliderAPI.id("pipeline/stamina_wheel"))
			.withVertexShader(ParagliderAPI.id("position_tex_color_flat"))
			.withFragmentShader(ParagliderAPI.id("position_tex_color_flat"))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.build();

	public static final RenderPipeline STAMINA_WHEEL_PIPELINE_DEBUG = RenderPipeline.builder(STAMINA_WHEEL_PIPELINE_SNIPPET)
			.withLocation(ParagliderAPI.id("pipeline/stamina_wheel_debug"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.withPolygonMode(PolygonMode.WIREFRAME)
			.build();

	@SubscribeEvent
	public static void register(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(STAMINA_WHEEL_PIPELINE);
		event.registerPipeline(STAMINA_WHEEL_PIPELINE_DEBUG);
	}
}
