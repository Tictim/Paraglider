package tictim.paraglider.dialog.script;

import tictim.paraglider.dialog.parser.Label;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * GoTo with {@link Label label}. Builder only.
 */
public class GoToWithLabel implements Script{
	@Nullable private Label label;

	public GoToWithLabel(){
		this(null);
	}
	public GoToWithLabel(@Nullable Label label){
		this.label = label;
	}

	@Nullable public Label getLabel(){
		return label;
	}
	public void setLabel(@Nullable Label label){
		this.label = label;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitGoTo(new GoTo(Objects.requireNonNull(label).getIndex()));
	}

	@Override public String toString(){
		return "GoTo "+(label==null ? "Label ???" : label.toString());
	}
}
