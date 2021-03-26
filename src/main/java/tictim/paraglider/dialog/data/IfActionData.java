package tictim.paraglider.dialog.data;

import javax.annotation.Nullable;

public class IfActionData extends ScriptData{
	public String doAction;
	@Nullable public ScenarioData ifScenario;
	@Nullable public ScenarioData elseScenario;

	public IfActionData(String doAction){
		this.doAction = doAction;
	}

	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitIfAction(this);
	}
}
