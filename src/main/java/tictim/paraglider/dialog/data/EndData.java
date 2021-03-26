package tictim.paraglider.dialog.data;

public class EndData extends ScriptData{
	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitEnd(this);
	}
}
