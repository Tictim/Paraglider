package tictim.paraglider.dialog.script;

import net.minecraft.util.text.ITextComponent;
import tictim.paraglider.dialog.parser.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Choice with {@link Label label}. Builder only.
 */
public class ChoiceWithLabel extends Text{
	private final List<Case> cases = new ArrayList<>();

	public ChoiceWithLabel(ITextComponent text){
		super(text);
	}

	public List<Case> getCases(){
		return cases;
	}

	public void addCase(ITextComponent text, Label then){
		cases.add(new Case(text, then));
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitChoice(new Choice(getText(), cases.stream().map(Case::toChoiceDialogCase).collect(Collectors.toList())));
	}

	@Override public String toString(){
		return "ChoiceText \""+getText().getString()+"\" "+cases.size()+": "+cases.stream().map(Case::toString).collect(Collectors.joining(", "));
	}

	public static final class Case{
		private final ITextComponent text;
		private final Label then;

		public Case(ITextComponent text, Label then){
			this.text = text;
			this.then = then;
		}

		public ITextComponent getText(){
			return text;
		}
		public Label getThen(){
			return then;
		}

		public Choice.Case toChoiceDialogCase(){
			return new Choice.Case(text, then.getIndex());
		}

		@Override public String toString(){
			return "Case \""+text.getString()+"\" : "+then;
		}
	}
}
