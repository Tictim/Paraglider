package tictim.paraglider.dialog.data;

public class GoToData extends ScriptData{
	public String go;

	public GoToData(String go){
		this.go = go;
	}

	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitGoTo(this);
	}
}
