package tictim.paraglider.client.dialog;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import tictim.paraglider.client.DialogScreen;

public class NextArrowWidget extends ArrowWidget{
	private static final long INPUT_DELAY = 250;

	public NextArrowWidget(DialogScreen screen){
		super(screen);
	}

	public boolean isNextAvailable(){
		return getState()==NextArrowWidget.State.VISIBLE&&WidgetUtils.pct(getStateTimestamp(), INPUT_DELAY)>=1;
	}

	@Override protected void drawShape(MatrixStack matrixStack, float offset, float scale, float alpha){
		if(alpha<=0) return;

		matrixStack.push();
		matrixStack.translate(screen.width/2, screen.height-30+4+offset, 0);
		matrixStack.scale(scale, scale, 1);

		screen.getMinecraft().textureManager.bindTexture(TEXTURE);

		Matrix4f matrix = matrixStack.getLast().getMatrix();
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
		// UV coords are little funny because I've noticed weird shit during debug
		bufferbuilder.pos(matrix, -TEXTURE_SIZE/2f, TEXTURE_SIZE/2f, 0).color(1, 1, 1, alpha).tex(0, 1).endVertex();
		bufferbuilder.pos(matrix, TEXTURE_SIZE/2f, TEXTURE_SIZE/2f, 0).color(1, 1, 1, alpha).tex(1, 1).endVertex();
		bufferbuilder.pos(matrix, TEXTURE_SIZE/2f, -TEXTURE_SIZE/2f, 0).color(1, 1, 1, alpha).tex(1, 0).endVertex();
		bufferbuilder.pos(matrix, -TEXTURE_SIZE/2f, -TEXTURE_SIZE/2f, 0).color(1, 1, 1, alpha).tex(0, 0).endVertex();
		bufferbuilder.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(bufferbuilder);

		matrixStack.pop();
	}
}
