package tictim.paraglider.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ParagliderRenderTypes {
	private ParagliderRenderTypes() {}

	public static final Function<ResourceLocation, RenderType> STAMINA_WHEEL = Util.memoize(
			texture -> RenderType.create(
					"paraglider_stamina_wheel",
					DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLE_FAN,
					1536, false, false,
					RenderType.CompositeState.builder()
							.setShaderState(RenderType.RENDERTYPE_GUI_OVERLAY_SHADER)
							.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
							.setDepthTestState(RenderType.NO_DEPTH_TEST)
							.setWriteMaskState(RenderStateShard.COLOR_WRITE)
							.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
							.createCompositeState(false)
			)
	);
}
