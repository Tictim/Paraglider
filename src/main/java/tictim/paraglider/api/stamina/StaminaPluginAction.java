package tictim.paraglider.api.stamina;

import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface StaminaPluginAction {
	record ProvideStaminaFactory(StaminaFactory factory) implements StaminaPluginAction {
		@Override public String toString() {
			return "ProvideStaminaFactory"; // prevent printing out gibberish by omitting the factory field
		}
	}
}
