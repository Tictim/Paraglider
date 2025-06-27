package tictim.paraglider.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.function.Function;

import static net.minecraft.client.renderer.RenderPipelines.MATRICES_COLOR_SNIPPET;

public final class ParagliderRenderTypes {
	private ParagliderRenderTypes() {}

	public static final RenderPipeline STAMINA_WHEEL_PIPELINE = RenderPipeline.builder(MATRICES_COLOR_SNIPPET)
			.withLocation("pipeline/paraglider/idk_man_what_the_fuck_is_this")
			.withVertexShader("core/position_tex_color")
			.withFragmentShader("core/position_tex_color")
			.withSampler("Sampler0")
			.withBlend(BlendFunction.TRANSLUCENT)
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withDepthWrite(false)
			.build();

	public static final Function<ResourceLocation, RenderType> STAMINA_WHEEL = Util.memoize(
			texture -> RenderType.create(
					"paraglider_stamina_wheel",
					1536,
					STAMINA_WHEEL_PIPELINE,
					RenderType.CompositeState.builder()
							.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.DEFAULT, false))
							.createCompositeState(false)
			)
	);
}

