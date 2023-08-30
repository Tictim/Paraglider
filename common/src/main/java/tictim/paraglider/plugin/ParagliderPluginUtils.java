package tictim.paraglider.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ParagliderPluginBase;
import tictim.paraglider.api.plugin.PluginAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ParagliderPluginUtils{
	private ParagliderPluginUtils(){}

	@NotNull public static <T> List<T> removeAll(@NotNull Collection<T> collection, @NotNull Predicate<T> condition){
		List<T> newList = new ArrayList<>();
		for(var it = collection.iterator(); it.hasNext(); ){
			T t = it.next();
			if(condition.test(t)){
				it.remove();
				newList.add(t);
			}
		}
		return newList;
	}

	/**
	 * @param resolverGetter     Provider for conflict resolver
	 * @param conflictingActions List of conflicting actions
	 * @param <P>                Type of the plugin
	 * @param <A>                Type of the action
	 * @return List of proceeded actions, or {@code null} if it should error the fuck out
	 */
	@Nullable public static <P extends ParagliderPluginBase, A> List<@NotNull PluginAction<P, A>> resolve(
			@NotNull Function<P, ConflictResolver<P, ? super A>> resolverGetter,
			@NotNull List<@NotNull PluginAction<P, A>> conflictingActions
	){
		if(conflictingActions.isEmpty()) return null;
		if(conflictingActions.size()==1) return List.of(conflictingActions.get(0));

		@SuppressWarnings("unchecked")
		PluginAction<P, A>[] arr = conflictingActions.toArray(new PluginAction[0]);
		List<PluginAction<P, A>> proceededActions = new ArrayList<>();
		for(int i = 0; i<arr.length; i++){
			PluginAction<P, A> action = arr[i];

			@SuppressWarnings("unchecked")
			PluginAction<P, A>[] arr2 = new PluginAction[arr.length-1];
			if(i>0) System.arraycopy(arr, 0, arr2, 0, i);
			if(i<arr.length-1) System.arraycopy(arr, i+1, arr2, i, arr.length-i-1);

			ConflictResolver<P, ? super A> resolver = resolverGetter.apply(action.plugin().instance());
			Objects.requireNonNull(resolver, "Plugin instance "+action.plugin()+" provided null as ConflictResolver");
			ConflictResolver.Resolution resolution = resolver.resolveConflict(action.action(), arr2);
			Objects.requireNonNull(resolution, "ConflictResolver provided by plugin instance "+action.plugin()+" returned null");
			switch(resolution){
				case PROCEED -> proceededActions.add(action);
				case ABORT -> {}
				case ERROR -> {
					return null;
				}
			}
		}
		return proceededActions;
	}

	@NotNull public static <P extends ParagliderPluginBase, A> RuntimeException composePluginLoadingError(
			@NotNull List<@NotNull PluginAction<P, A>> conflictingActions
	){
		return new RuntimeException("Cannot continue loading paraglider plugins due to conflicting actions between plugins\n  "
				+conflictingActions.stream()
				.map(pa -> pa.action()+" by "+
						pa.plugin().instance()+
						(pa.plugin().modid()!=null ? " (mod "+pa.plugin().modid()+")" : ""))
				.collect(Collectors.joining("\n  ")));
	}
}
