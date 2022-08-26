package tictim.paraglider.contents.recipe.bargain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BargainResult{
	public static BargainResult success(){
		return new BargainResult(Collections.emptySet());
	}
	public static BargainResult failure(FailedReason... reasons){
		Set<FailedReason> set = new HashSet<>();
		Collections.addAll(set, reasons);
		return new BargainResult(set);
	}
	public static BargainResult result(Set<FailedReason> reasons){
		return new BargainResult(reasons);
	}

	private final Set<FailedReason> failedReasons;

	private BargainResult(Set<FailedReason> failedReasons){
		this.failedReasons = failedReasons;
	}

	public boolean isSuccess(){
		return failedReasons.isEmpty();
	}

	public Set<FailedReason> getFailureReasons(){
		return failedReasons;
	}

	public boolean has(FailedReason reason){
		return failedReasons.contains(reason);
	}

	@Override public String toString(){
		return "BargainResult{"+
				"failedReasons="+failedReasons+
				'}';
	}

	public enum FailedReason{
		NOT_ENOUGH_ITEMS,
		NOT_ENOUGH_HEART,
		NOT_ENOUGH_STAMINA,
		NOT_ENOUGH_ESSENCE,
		HEART_FULL,
		STAMINA_FULL,
		ESSENCE_FULL,
		OTHER
	}
}
