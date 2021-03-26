package tictim.paraglider.dialog;

import java.util.Objects;

/**
 * Server side actions used in dialog.<br>
 * Note that the client can request ANY actions defined in same dialog,
 * so it's critical to validate request before applying any modifications to game state.
 */
public final class DialogAction{
	private final String id;
	private final Function action;

	public DialogAction(String id, Function action){
		this.id = Objects.requireNonNull(id);
		this.action = Objects.requireNonNull(action);
	}

	public String getId(){
		return id;
	}
	public Function getAction(){
		return action;
	}

	@Override public boolean equals(Object o){
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		DialogAction that = (DialogAction)o;
		return id.equals(that.id);
	}
	@Override public int hashCode(){
		return Objects.hash(id);
	}

	@Override public String toString(){
		return "DialogAction "+id;
	}

	public interface Function{
		void perform(DialogActionArgs args) throws DialogActionException;
	}
}
