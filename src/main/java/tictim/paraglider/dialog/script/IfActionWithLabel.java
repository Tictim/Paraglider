package tictim.paraglider.dialog.script;

import tictim.paraglider.dialog.parser.Label;

import javax.annotation.Nullable;

/**
 * IfAction with {@link Label label}. Builder only.
 */
public class IfActionWithLabel implements Script{
	private final String action;
	@Nullable private Label elseThen;

	public IfActionWithLabel(String action){
		this.action = action;
	}

	public String getAction(){
		return action;
	}

	@Nullable public Label getElseThen(){
		return elseThen;
	}
	public void setElseThen(@Nullable Label elseThen){
		this.elseThen = elseThen;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitIfAction(new IfAction(action, elseThen==null ? -1 : elseThen.getIndex()));
	}

	@Override public String toString(){
		return "IfAction "+action+" Else Then "+elseThen;
	}
}
