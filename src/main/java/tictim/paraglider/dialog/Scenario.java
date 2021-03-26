package tictim.paraglider.dialog;

import tictim.paraglider.dialog.script.Script;

import java.util.Arrays;

/**
 * It's just unmodifiable array of scripts.
 */
public final class Scenario{
	private final Script[] scripts;

	public Scenario(Script[] scripts){
		this.scripts = scripts;
	}

	public Script getScript(int index){
		return scripts[index];
	}
	public int size(){
		return scripts.length;
	}

	@Override public String toString(){
		return "Scenario{"+
				"scripts="+Arrays.toString(scripts)+
				'}';
	}
}
