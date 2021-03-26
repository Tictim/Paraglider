package tictim.paraglider.dialog.script;

public final class GoTo implements Script{
	private final int pos;

	public GoTo(int pos){
		this.pos = pos;
	}

	public int getPos(){
		return pos;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitGoTo(this);
	}

	@Override public String toString(){
		return "GoTo "+pos;
	}
}
