package tictim.paraglider.api.stamina;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.StaminaEfficiencyAttribute;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.plugin.ParagliderPluginBase;

import java.util.function.Supplier;

/**
 * Plugin for stamina-related features.
 *
 * @see ParagliderPlugin
 */
@NullMarked
public interface StaminaPlugin extends ParagliderPluginBase {
	/**
	 * @return A factory of {@link Stamina} interface. Any nonnull object returned by this method will replace default
	 * implementation of BotW-like stamina system with the implementation provided.<br/>
	 * If two or more plugins attempt to provide stamina implementation, it will create a conflict; see
	 * {@link ConflictResolver}.
	 */
	default @Nullable StaminaFactory getStaminaFactory() {
		return null;
	}

	default void registerStaminaEfficiencyLogic(StaminaEfficiencyLogicRegister register) {}

	/**
	 * @return Return {@code true} on client side to remove stamina wheel widget from the game. This is not a flag to
	 * disable stamina system as a whole. Paraglider assumes an external indicator for stamina state will be present,
	 * e.g. wiring Paraglider's stamina backend with external stamina implementation.
	 */
	default boolean removeStaminaWheel() {
		return false;
	}

	/**
	 * @return Implementation of {@link ConflictResolver} for this {@link StaminaPlugin} instance
	 */
	default ConflictResolver<StaminaPlugin, StaminaPluginAction> getStaminaPluginConflictResolver() {
		return ConflictResolver.proceed();
	}

	/**
	 * Interface for registering stamina efficiency logic.
	 */
	interface StaminaEfficiencyLogicRegister {
		/**
		 * Register stamina efficiency logic instance.
		 *
		 * @param logic Logic
		 * @throws NullPointerException If {@code logic == null}
		 */
		void register(StaminaEfficiencyLogic logic);

		/**
		 * Register stamina efficiency logic using attributes. Note that in addition to the condition supplied, whether
		 * player has the attribute is also checked.
		 *
		 * @param attribute Attribute
		 * @param condition Condition to apply this attribute
		 * @throws NullPointerException If {@code attribute == null || condition == null}
		 * @see StaminaEfficiencyAttribute
		 */
		void registerAttribute(Supplier<Holder<Attribute>> attribute, AttributeEfficiencyCondition condition);
	}

	@FunctionalInterface
	interface AttributeEfficiencyCondition {
		boolean isApplicable(double baseStaminaDelta,
		                     StaminaEfficiencyLogic.Context context,
		                     Player player);
	}
}
