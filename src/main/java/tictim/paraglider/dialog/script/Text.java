package tictim.paraglider.dialog.script;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

public class Text implements Script{
	private final ITextComponent text;

	public Text(ITextComponent text){
		this.text = Objects.requireNonNull(text);
	}

	public ITextComponent getText(){
		return text;
	}

	@Override public void visit(ScriptVisitor visitor){
		visitor.visitText(this);
	}

	@Override public String toString(){
		return "Text "+textComponentToString(text);
	}

	public static String textComponentToString(ITextComponent text){
		if(text instanceof StringTextComponent){
			return '"'+((StringTextComponent)text).getText()+'"';
		}else if(text instanceof TranslationTextComponent){
			return "lang:"+((TranslationTextComponent)text).getKey();
		}else return text.toString();
	}
}
