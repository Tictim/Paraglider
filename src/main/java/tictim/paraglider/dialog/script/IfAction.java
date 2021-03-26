package tictim.paraglider.dialog.script;

public class IfAction implements Script{
	private final String action;
	private final int elseThen;

	public IfAction(String action, int elseThen){
		this.action = action;
		this.elseThen = elseThen;
	}

	public String getAction(){
		return action;
	}

	/**
	 * @return GOTO index when result of Predicate comes out {@code false}. Value doesn't exists when {@code value < 0}.
	 */
	public int getElseThen(){
		return elseThen;
	}

	public boolean doesElseThenExists(){
		return elseThen>=0;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitIfAction(this);
	}

	@Override public String toString(){
		return "IfAction "+action+" Else Then "+elseThen;
	}
}
