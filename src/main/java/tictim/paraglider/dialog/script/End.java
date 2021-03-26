package tictim.paraglider.dialog.script;

public enum End implements Script{
	INSTANCE;

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitEnd();
	}

	@Override public String toString(){
		return "End";
	}
}
