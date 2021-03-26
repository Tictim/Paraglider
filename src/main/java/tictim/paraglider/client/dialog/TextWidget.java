package tictim.paraglider.client.dialog;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import tictim.paraglider.client.DialogScreen;

import javax.annotation.Nullable;
import java.util.Set;

public final class TextWidget{
	private static final long TEXT_APPEAR_TIME = 100;
	private static final long TEXT_UPDATE_INTERVAL = 500;
	private static final long FADEOUT_TIME = 150;

	private static final Set<String> TEXT_CENTERED_LANGUAGES = ImmutableSet.of(
			"ja_jp",
			"ko_kr"
	);

	private final DialogScreen screen;

	@Nullable private TextUnit[] text = null;
	private int textDisplayIndex;

	@Nullable private TransitionState transitionState;

	private final boolean centerText;

	public TextWidget(DialogScreen screen){
		this.screen = screen;

		this.centerText = TEXT_CENTERED_LANGUAGES.contains(Minecraft.getInstance().gameSettings.language);
	}

	public void render(MatrixStack matrixStack){
		if(transitionState!=null){
			transitionState.onRender(matrixStack);
			if(transitionState.isFinished())
				transitionState = null;
		}else{
			drawText(matrixStack, 1);
		}
	}

	private void drawText(MatrixStack matrixStack, float alpha){
		if(text==null||alpha<=0) return;

		FontRenderer font = screen.getMinecraft().fontRenderer;

		int lines = this.text[this.text.length-1].getLines();
		int color = WidgetUtils.argb(alpha, 0xFFFFFF);

		int yStart;
		switch(lines){
			case 1:
				yStart = font.FONT_HEIGHT*-2;
				break;
			case 2:
				yStart = Math.round(font.FONT_HEIGHT*-2.5f);
				break;
			default:
				yStart = -font.FONT_HEIGHT*lines;
		}

		matrixStack.push();
		//noinspection IntegerDivisionInFloatingPointContext
		matrixStack.translate(screen.width/2, screen.height-30+yStart, 0);

		if(centerText){
			for(int i = 0; i<this.text[textDisplayIndex].getLines(); i++){
				String s = this.text[textDisplayIndex].getText(i);
				AbstractGui.drawString(matrixStack, font, s, -this.text[this.text.length-1].getWidth(i, font)/2, i*font.FONT_HEIGHT, color);
			}
		}else{
			int x = -this.text[this.text.length-1].getMaxWidth(font)/2;

			for(int i = 0; i<this.text[textDisplayIndex].getLines(); i++){
				String s = this.text[textDisplayIndex].getText(i);
				AbstractGui.drawString(matrixStack, font, s, x, i*font.FONT_HEIGHT, color);
			}
		}

		matrixStack.pop();
	}

	public Transition fadeOut(){
		if(transitionState!=null&&!transitionState.isFinished()){
			throw new IllegalStateException("Another transition is in progress.");
		}
		transitionState = new FadeOut();
		return transitionState.transition;
	}
	public Transition show(String text){
		if(transitionState!=null&&!transitionState.isFinished()){
			throw new IllegalStateException("Another transition is in progress.");
		}
		this.text = TextUnit.parse(text, screen.getContainer());
		this.textDisplayIndex = 0;
		this.transitionState = new ShowText();
		return transitionState.transition;
	}

	private static class TransitionState{
		public final Transition transition = new Transition();
		protected final long startTimestamp = System.currentTimeMillis();

		public void onRender(MatrixStack matrixStack){}

		public boolean isFinished(){
			return transition.isFinished();
		}
	}

	private class FadeOut extends TransitionState{
		@Override public void onRender(MatrixStack matrixStack){
			float pct = WidgetUtils.pct(startTimestamp, FADEOUT_TIME);
			if(pct<1) drawText(matrixStack, 1-pct);
			else{
				text = null;
				transition.finish();
			}
		}
	}

	private class ShowText extends TransitionState{
		private long textAppendTimestamp = startTimestamp+TEXT_UPDATE_INTERVAL;

		@Override public void onRender(MatrixStack matrixStack){
			long t = System.currentTimeMillis();
			if(textAppendTimestamp<=t) advance(t);
			drawText(matrixStack, WidgetUtils.pct(startTimestamp, TEXT_APPEAR_TIME));
		}

		private void advance(long t){
			if(text==null||text.length-1<=textDisplayIndex) transition.finish();
			else{
				textDisplayIndex++;
				textAppendTimestamp = t+TEXT_UPDATE_INTERVAL;
			}
		}
	}
}
