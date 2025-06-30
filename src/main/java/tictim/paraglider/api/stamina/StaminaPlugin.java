package tictim.paraglider.api.stamina;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

	default void registerStaminaEfficiencyLogic(@NotNull StaminaEfficiencyLogicRegister register) {}

	/**
	 * @return Implementation of {@link ConflictResolver} for this {@link StaminaPlugin} instance
	 */
	default @NotNull ConflictResolver<StaminaPlugin, StaminaPluginAction> getStaminaPluginConflictResolver() {
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
		void register(@NotNull StaminaEfficiencyLogic logic);

		/**
		 * Register stamina efficiency logic using attributes. Note that in addition to the condition supplied, whether
		 * player has the attribute is also checked.
		 *
		 * @param attribute Attribute
		 * @param condition Condition to apply this attribute
		 * @throws NullPointerException If {@code attribute == null || condition == null}
		 * @see StaminaEfficiencyAttribute
		 */
		void registerAttribute(@NotNull Supplier<Holder<Attribute>> attribute, @NotNull AttributeEfficiencyCondition condition);
	}

	@FunctionalInterface
	interface AttributeEfficiencyCondition {
		boolean isApplicable(double baseStaminaDelta,
		                     @NotNull StaminaEfficiencyLogic.Context context,
		                     @NotNull Player player);
	}
}
