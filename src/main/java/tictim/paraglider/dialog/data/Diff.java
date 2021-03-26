package tictim.paraglider.dialog.data;

public class Diff{
	public Action action;
	public String label;
	public ScenarioData insert;

	public Diff(Action action, String label, ScenarioData insert){
		this.action = action;
		this.label = label;
		this.insert = insert;
	}

	public enum Action{
		BEFORE, AFTER, REPLACE
	}
}
