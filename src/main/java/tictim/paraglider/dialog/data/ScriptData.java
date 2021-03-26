package tictim.paraglider.dialog.data;

import javax.annotation.Nullable;

public abstract class ScriptData{
	@Nullable public String label;

	public abstract void accept(ScriptDataVisitor visitor);
}
