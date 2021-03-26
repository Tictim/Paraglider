package tictim.paraglider.client.dialog;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import tictim.paraglider.client.DialogScreen;

import static tictim.paraglider.ParagliderMod.MODID;

public abstract class ArrowWidget{
	protected static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/dialog_next.png");
	protected static final int TEXTURE_SIZE = 16;

	protected final DialogScreen screen;
	private State state = State.INVISIBLE;
	/**
	 * Timestamp for start of current state
	 */
	private long stateTimestamp;
	/**
	 * Timestamp since visible
	 */
	private long visibleTimestamp;

	public ArrowWidget(DialogScreen screen){
		this.screen = screen;
	}

	protected State getState(){
		return this.state;
	}
	protected long getStateTimestamp(){
		return this.stateTimestamp;
	}
	protected long getVisibleTimestamp(){
		return this.visibleTimestamp;
	}

	public long getAppearTime(){
		return 50;
	}
	public long getDisappearTime(){
		return 150;
	}
	public long getIdleHalfCycle(){
		return 300;
	}
	public float getOscillation(){
		return 2;
	}

	public void render(MatrixStack matrixStack){
		if(!state.isVisible()) return;

		float offset = WidgetUtils.linearOscillation(getVisibleTimestamp()-System.currentTimeMillis(), getIdleHalfCycle(), 0, getOscillation());
		float scale = state==State.VISIBLE||state==State.BOOP ? MathHelper.lerp(WidgetUtils.pct(stateTimestamp, getDisappearTime()), 0.5f, 1) : 1;
		float alpha = getAlpha();

		drawShape(matrixStack, offset, scale, alpha);
	}

	protected abstract void drawShape(MatrixStack matrixStack, float offset, float scale, float alpha);

	private float getAlpha(){
		switch(state){
			case INVISIBLE:
				return 0;
			case VISIBLE:
				return WidgetUtils.pct(stateTimestamp, getAppearTime());
			case BOOP:
				return 1;
			default:
				throw new IllegalStateException("Unreachable");
		}
	}

	private void setState(State state){
		if(this.state!=state){
			State prev = this.state;
			this.state = state;
			this.stateTimestamp = System.currentTimeMillis();
			if(!prev.isVisible()&&state.isVisible()){
				this.visibleTimestamp = System.currentTimeMillis();
			}
		}
	}

	public void show(){
		setState(State.VISIBLE);
	}

	public void boop(){
		if(this.state.isVisible())
			setState(State.BOOP);
	}

	public void hide(){
		setState(State.INVISIBLE);
	}

	public enum State{
		INVISIBLE,
		VISIBLE,
		BOOP;

		public boolean isVisible(){
			return this!=INVISIBLE;
		}
	}
}
