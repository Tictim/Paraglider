package tictim.paraglider.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import tictim.paraglider.recipe.bargain.BargainResult;
import tictim.paraglider.recipe.bargain.StatueBargain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;

public final class StatueDialog{
	private final Map<Case, Dialogs> map = new EnumMap<>(Case.class);

	public StatueDialog(){
		for(Case c : Case.values()) map.put(c, new Dialogs());
	}

	public StatueDialog atInitial(String translationKey){
		return add(Case.INITIAL, new TranslatableComponent(translationKey));
	}
	public StatueDialog atSuccess(String translationKey){
		return add(Case.BARGAIN_SUCCESS, new TranslatableComponent(translationKey));
	}
	public StatueDialog atSuccess(String translationKey, @Nullable BiPredicate<StatueBargain, BargainResult> predicate){
		return add(Case.BARGAIN_SUCCESS, new TranslatableComponent(translationKey), predicate);
	}
	public StatueDialog atSuccessFallback(String translationKey){
		return setFallback(Case.BARGAIN_SUCCESS, new TranslatableComponent(translationKey));
	}
	public StatueDialog atFailure(String translationKey){
		return add(Case.BARGAIN_FAILURE, new TranslatableComponent(translationKey));
	}
	public StatueDialog atFailure(String translationKey, @Nullable BiPredicate<StatueBargain, BargainResult> predicate){
		return add(Case.BARGAIN_FAILURE, new TranslatableComponent(translationKey), predicate);
	}
	public StatueDialog atFailureFallback(String translationKey){
		return setFallback(Case.BARGAIN_FAILURE, new TranslatableComponent(translationKey));
	}

	public StatueDialog add(Case dialogCase, Component text){
		return add(dialogCase, text, null);
	}
	public StatueDialog add(Case dialogCase, Component text, @Nullable BiPredicate<StatueBargain, BargainResult> predicate){
		map.get(dialogCase).dialog.add(new Dialog(predicate, Objects.requireNonNull(text)));
		return this;
	}

	public StatueDialog setFallback(Case dialogCase, Component text){
		return setFallback(dialogCase, text, null);
	}
	public StatueDialog setFallback(Case dialogCase, Component text, @Nullable BiPredicate<StatueBargain, BargainResult> predicate){
		map.get(dialogCase).fallback = new Dialog(predicate, Objects.requireNonNull(text));
		return this;
	}

	@Nullable public Component getDialog(Random random, Case dialogCase, @Nullable StatueBargain bargain, @Nullable BargainResult result){
		return map.get(dialogCase).getDialog(random, bargain, result);
	}

	private static final class Dialogs{
		private final List<Dialog> dialog = new ArrayList<>();
		@Nullable private Dialog fallback;

		@Nullable public Component getDialog(Random random, @Nullable StatueBargain bargain, @Nullable BargainResult bargainResult){
			List<Dialog> dialogs = new ArrayList<>();
			for(Dialog f : dialog)
				if(bargain==null||bargainResult==null||f.predicate==null||f.predicate.test(bargain, bargainResult))
					dialogs.add(f);
			Dialog chosenDialog = dialogs.isEmpty() ? fallback : dialogs.get(random.nextInt(dialogs.size()));
			return chosenDialog!=null ? chosenDialog.text : null;
		}
	}

	private record Dialog(@Nullable BiPredicate<StatueBargain, BargainResult> predicate, Component text){}

	public enum Case{
		INITIAL,
		BARGAIN_SUCCESS,
		BARGAIN_FAILURE
	}
}
