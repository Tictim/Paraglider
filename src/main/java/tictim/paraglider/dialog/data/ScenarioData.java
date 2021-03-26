package tictim.paraglider.dialog.data;

import java.util.ArrayList;
import java.util.List;

public final class ScenarioData extends ScriptData{
	public final List<ScriptData> scenario = new ArrayList<>();

	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitScenario(this);
	}
}
