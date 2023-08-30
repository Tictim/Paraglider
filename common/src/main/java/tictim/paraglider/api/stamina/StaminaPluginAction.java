package tictim.paraglider.api.stamina;

import org.jetbrains.annotations.NotNull;

public sealed interface StaminaPluginAction{
	record ProvideStaminaFactory(@NotNull StaminaFactory factory) implements StaminaPluginAction{
		@Override public String toString(){
			return "ProvideStaminaFactory"; // prevent printing out gibberish by omitting the factory field
		}
	}
}
