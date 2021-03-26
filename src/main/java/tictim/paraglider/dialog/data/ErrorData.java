package tictim.paraglider.dialog.data;

public class ErrorData extends ScriptData{
	public String error;

	public ErrorData(String error){
		this.error = error;
	}

	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitError(this);
	}
}
