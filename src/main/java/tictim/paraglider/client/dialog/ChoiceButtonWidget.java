package tictim.paraglider.client.dialog;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import tictim.paraglider.client.DialogScreen;
import tictim.paraglider.dialog.script.Choice;

import javax.annotation.Nullable;

public class ChoiceButtonWidget{
	private static final long APPEAR_TRANSITION_TIME = 200;
	private static final long SELECT_TRANSITION_TIME = 200;
	private static final int VERTICAL_MARGIN = 4;
	private static final int HORIZONTAL_MARGIN = 8;

	private final DialogScreen screen;
	private final Choice choice;
	private final int index;

	private final long renderStartTimestamp;

	private boolean onCursor;

	@Nullable private Transition selectTransition;
	private long transitionStartTimestamp;

	public ChoiceButtonWidget(DialogScreen screen, Choice choice, int index, long renderStartTimestamp){
		this.screen = screen;
		this.choice = choice;
		this.index = index;
		this.renderStartTimestamp = renderStartTimestamp;
	}

	public int getIndex(){
		return index;
	}
	public Choice.Case getCase(){
		return choice.getCases().get(index);
	}

	public void render(MatrixStack matrixStack){
		if(renderStartTimestamp>System.currentTimeMillis()) return;

		double x = getRight();
		double y = getBottom();

		matrixStack.push();
		matrixStack.translate(x, y, 0);
		FontRenderer font = screen.getMinecraft().fontRenderer;

		AbstractGui.fill(matrixStack, -getWidth()+1, -getHeight()+1, -1, -1, 0xAA000000);
		if(selectTransition!=null){
			float pct = WidgetUtils.pct(transitionStartTimestamp, SELECT_TRANSITION_TIME);
			if(pct>=1){
				selectTransition.finish();
				selectTransition = null;
			}else AbstractGui.fill(matrixStack, -getWidth()+1, -getHeight()+1, -1, -1,
					WidgetUtils.argb(1-pct, 0xFFFFFF));
		}

		int borderColor = onCursor ? 0xFFFFFFFF : 0xAA808080;
		AbstractGui.fill(matrixStack, -getWidth(), -getHeight(), -getWidth()+1, 0, borderColor);
		AbstractGui.fill(matrixStack, -1, -getHeight(), 0, 0, borderColor);
		AbstractGui.fill(matrixStack, -getWidth()+1, -getHeight(), -1, -getHeight()+1, borderColor);
		AbstractGui.fill(matrixStack, -getWidth()+1, -1, -1, 0, borderColor);

		AbstractGui.drawString(matrixStack, font, getCase().getText().getString(), -getWidth()+HORIZONTAL_MARGIN, -font.FONT_HEIGHT-VERTICAL_MARGIN, 0xFFFFFFFF);
		// TODO scissor

		matrixStack.pop();
	}

	public double getRight(){
		return screen.width-10-Math.round(5*WidgetUtils.pct(renderStartTimestamp, APPEAR_TRANSITION_TIME));
	}
	public double getBottom(){
		return screen.height-40-(choice.getCases().size()-1-index)*20;
	}

	public int getWidth(){
		return 100;
	}
	public int getHeight(){
		return screen.getMinecraft().fontRenderer.FONT_HEIGHT+VERTICAL_MARGIN*2;
	}

	public double getLeft(){
		return getRight()-getWidth();
	}
	public double getTop(){
		return getBottom()-getHeight();
	}

	public boolean isOnCursor(){
		return onCursor;
	}
	public void setOnCursor(boolean onCursor){
		this.onCursor = onCursor;
	}

	public boolean isCursorOn(double mouseX, double mouseY){
		return mouseX>=getLeft()&&mouseX<getRight()&&mouseY>=getTop()&&mouseY<getBottom();
	}

	public Transition playSelectedAnimation(){
		if(selectTransition!=null) throw new IllegalStateException("Another transition is in progress.");
		selectTransition = new Transition();
		transitionStartTimestamp = System.currentTimeMillis();
		return selectTransition;
	}
}
