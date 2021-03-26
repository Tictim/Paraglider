package tictim.paraglider.dialog.script;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class Choice extends Text{
	private final List<Case> cases;

	public Choice(ITextComponent text, List<Case> cases){
		super(text);
		this.cases = cases;
	}

	public List<Case> getCases(){
		return cases;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitChoice(this);
	}

	@Override public String toString(){
		return "ChoiceText "+textComponentToString(getText());
	}

	public static final class Case{
		private final ITextComponent text;
		private final int then;

		public Case(ITextComponent text, int then){
			this.text = text;
			this.then = then;
		}

		public ITextComponent getText(){
			return text;
		}
		public int getThen(){
			return then;
		}

		@Override public String toString(){
			return "Case "+textComponentToString(text)+" : "+then;
		}
	}
}
