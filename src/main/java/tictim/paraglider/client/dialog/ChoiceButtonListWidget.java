package tictim.paraglider.client.dialog;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.matrix.MatrixStack;
import tictim.paraglider.client.DialogScreen;
import tictim.paraglider.dialog.script.Choice;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChoiceButtonListWidget{
	private static final long APPEAR_TIME_DEVIATION = 100;

	private final DialogScreen screen;

	private final ChoiceArrowWidget arrow;

	private List<ChoiceButtonWidget> buttons = new ArrayList<>();
	private int lockedButton;

	public ChoiceButtonListWidget(DialogScreen screen){
		this.screen = screen;
		this.arrow = new ChoiceArrowWidget(screen);
		this.arrow.show();
	}

	public void render(MatrixStack matrixStack, int mouseX, int mouseY){
		for(ChoiceButtonWidget b : buttons){
			if(lockedButton<0){
				boolean cursorOn = b.isCursorOn(mouseX, mouseY);
				b.setOnCursor(cursorOn);
			}
			b.render(matrixStack);
			if(b.isOnCursor()){
				arrow.setButton(b);
				arrow.render(matrixStack);
			}
		}
	}

	@Nullable public ChoiceButtonWidget getButtonAt(double mouseX, double mouseY){
		for(ChoiceButtonWidget button : buttons){
			if(button.isCursorOn(mouseX, mouseY)) return button;
		}
		return null;
	}

	public List<ChoiceButtonWidget> buttons(){
		return buttons;
	}

	public void clear(){
		if(!buttons.isEmpty())
			buttons = new ArrayList<>(); // Funny hack to prevent CMEs. Who cares, it works
		lockedButton = -1;
		arrow.setButton(null);
	}

	public void createButtons(Choice choice){
		clear();
		long t = System.currentTimeMillis();
		for(int i = 0; i<choice.getCases().size(); i++){
			buttons.add(new ChoiceButtonWidget(
					screen,
					choice,
					i,
					t+Math.round(APPEAR_TIME_DEVIATION*i/(double)choice.getCases().size())));
		}
	}

	public boolean acceptsInput(){
		return lockedButton<0;
	}

	@Nullable public Pair<Transition, ChoiceButtonWidget> clickButton(double mouseX, double mouseY){
		if(lockedButton>=0) return null;
		ChoiceButtonWidget buttonAt = getButtonAt(mouseX, mouseY);
		if(buttonAt==null) return null;
		lockedButton = buttonAt.getIndex();
		for(int i = 0; i<buttons.size(); i++){
			buttons.get(i).setOnCursor(lockedButton==i);
		}
		arrow.boop();
		return Pair.of(buttonAt.playSelectedAnimation(), buttonAt);
	}
}
