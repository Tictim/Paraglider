package tictim.paraglider.dialog.script;

public interface ScriptVisitor{
	void visitText(Text text);
	void visitChoice(Choice choice);
	void visitGoTo(GoTo goTo);
	void visitIfAction(IfAction ifAction);
	void visitEnd();
	void visitError(Error error);
}
