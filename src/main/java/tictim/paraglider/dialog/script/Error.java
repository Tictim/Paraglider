package tictim.paraglider.dialog.script;

public class Error implements Script{
	private final String message;

	public Error(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitError(this);
	}

	@Override public String toString(){
		return "Error \""+message+'"';
	}
}
