package tictim.paraglider.dialog.data;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ChoiceData{
	public final List<Case> cases = new ArrayList<>();

	public static final class Case{
		public String text;
		public ScenarioData scenario;
		@Nullable public String then;

		public Case(String text, ScenarioData scenario, @Nullable String then){
			this.text = text;
			this.scenario = scenario;
			this.then = then;
		}

		public ITextComponent convertText(){
			if(text.startsWith("lang:")){
				return new TranslationTextComponent(text.substring(5));
			}else return new StringTextComponent(text);
		}
		public ScriptEnding parseThen(){
			if(then==null){
				return ScriptEnding.ERROR;
			}else switch(then){
				case "error":
					return ScriptEnding.ERROR;
				case "end":
					return ScriptEnding.END;
				case "loop":
					return ScriptEnding.LOOP;
				case "continue":
					return ScriptEnding.CONTINUE;
				default:
					throw new IllegalStateException("Invalid \"then\" value: "+then);
			}
		}
	}

	public enum ScriptEnding{
		ERROR,
		END,
		LOOP,
		CONTINUE
	}
}
