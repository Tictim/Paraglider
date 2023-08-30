package tictim.paraglider.impl.stamina;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;

public final class NullStamina implements Stamina{
	private NullStamina(){}

	private static final NullStamina instance = new NullStamina();

	@NotNull public static NullStamina get(){
		return instance;
	}

	@Override public int stamina(){
		return 0;
	}
	@Override public void setStamina(int stamina){}
	@Override public int maxStamina(){
		return 0;
	}
	@Override public boolean isDepleted(){
		return false;
	}
	@Override public void setDepleted(boolean depleted){}
	@Override public void update(@NotNull Movement movement){}
	@Override public int giveStamina(int amount, boolean simulate){
		return 0;
	}
	@Override public int takeStamina(int amount, boolean simulate, boolean ignoreDepletion){
		return 0;
	}
}
