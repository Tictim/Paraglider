package tictim.paraglider.dialog.data;

public interface ScriptDataVisitor{
	void visitText(TextData textData);
	void visitIfAction(IfActionData ifActionData);
	void visitEnd(EndData endData);
	void visitError(ErrorData errorData);
	void visitGoTo(GoToData goToData);
	void visitScenario(ScenarioData scenarioData);
}
