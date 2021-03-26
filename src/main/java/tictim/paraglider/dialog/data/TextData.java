package tictim.paraglider.dialog.data;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public final class TextData extends ScriptData{
	public String dialog;
	@Nullable public ChoiceData choices;

	public TextData(String dialog){
		this.dialog = dialog;
	}

	@Override public void accept(ScriptDataVisitor visitor){
		visitor.visitText(this);
	}

	public ITextComponent convertDialog(){
		if(dialog.startsWith("lang:")){
			return new TranslationTextComponent(dialog.substring(5));
		}else return new StringTextComponent(dialog);
	}
}
