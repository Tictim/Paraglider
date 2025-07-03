package tictim.paraglider.plugin;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.movement.MovementPluginAction;
import tictim.paraglider.api.movement.MovementPluginAction.NewState;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ConflictResolver.Resolution;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.stamina.StaminaPlugin;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.wind.Wind;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.*;
import static tictim.paraglider.impl.movement.PlayerMovementValues.PARAGLIDING_FALL_DISTANCE;

@ParagliderPlugin
public class ParagliderDefaultPlugin implements MovementPlugin, StaminaPlugin {
	@Override public void registerNewStates(@NotNull PlayerStateRegister register) {
		register.register(IDLE, IDLE_STAMINA_DELTA);
		register.register(FLYING, FLYING_STAMINA_DELTA);
		register.register(CREATIVE_FLYING, CREATIVE_FLYING_STAMINA_DELTA);
		register.register(ELYTRA_FLYING, ELYTRA_FLYING_STAMINA_DELTA);
		register.register(ON_VEHICLE, ON_VEHICLE_STAMINA_DELTA);
		register.register(SWIMMING, SWIMMING_STAMINA_DELTA, Flags.DISABLED_BY_RUNNING_CONFIG, Flags.IS_SPRINTING, Flags.IS_UNDERWATER);
		register.register(UNDERWATER, UNDERWATER_STAMINA_DELTA, Flags.DISABLED_BY_RUNNING_CONFIG, Flags.IS_UNDERWATER);
		register.register(BREATHING_UNDERWATER, BREATHING_UNDERWATER_STAMINA_DELTA, Flags.DISABLED_BY_RUNNING_CONFIG, Flags.IS_UNDERWATER);
		register.register(PARAGLIDING, PARAGLIDING_STAMINA_DELTA, Flags.DISABLED_BY_PARAGLIDING_CONFIG, Flags.PARAGLIDING, Flags.IS_PARAGLIDING);
		register.register(PANIC_PARAGLIDING, PANIC_PARAGLIDING_STAMINA_DELTA, Flags.PARAGLIDING, Flags.DISABLED_BY_PARAGLIDING_CONFIG, Flags.IS_PARAGLIDING);
		register.register(ASCENDING, ASCENDING_STAMINA_DELTA, Flags.PARAGLIDING, Flags.ASCENDING, Flags.DISABLED_BY_PARAGLIDING_CONFIG, Flags.IS_PARAGLIDING);
		register.register(RUNNING, RUNNING_STAMINA_DELTA, Flags.DISABLED_BY_RUNNING_CONFIG, Flags.IS_SPRINTING);
		register.register(MIDAIR, MIDAIR_STAMINA_DELTA);
	}

	@Override public void registerStateConnections(@NotNull PlayerStateConnectionRegister register) {
		register.connect(IDLE, FLYING, c -> c.player().getAbilities().flying, FLYING_PRIORITY);

		register.connect(FLYING, CREATIVE_FLYING, c -> c.player().isCreative());

		register.connect(IDLE, ELYTRA_FLYING, c -> c.player().isFallFlying(), ELYTRA_FLYING_PRIORITY);
		register.connect(IDLE, ON_VEHICLE, c -> c.player().getVehicle() != null, ON_VEHICLE_PRIORITY);
		register.connect(IDLE, SWIMMING, c -> c.player().isSwimming(), SWIMMING_PRIORITY);
		register.connect(IDLE, UNDERWATER, c -> c.player().isInWater(), UNDERWATER_PRIORITY);
		register.connect(UNDERWATER, BREATHING_UNDERWATER, c -> ParagliderUtils.canBreatheUnderwater(c.player()));

		register.connect(IDLE, PARAGLIDING, c -> {
			if (c.player().onGround()) return false;
			if (!ParagliderUtils.canUseParaglider(c)) return false;
			ItemStack stack = c.player().getMainHandItem();
			return stack.is(ParagliderTags.PARAGLIDERS) && ParagliderUtils.getCaps(stack).canDoParagliding(c.player(), stack);
		}, PARAGLIDING_PRIORITY);

		register.connect(PARAGLIDING, PANIC_PARAGLIDING, c -> c.accumulatedFallDistance() >= PARAGLIDING_FALL_DISTANCE && c.stamina().isDepleted());
		register.connect(PARAGLIDING, ASCENDING, c -> Cfg.get().updraft() && Wind.getWindAbove(c.player().level(), c.player().getBoundingBox()) > 0);
		register.connect(PARAGLIDING, IDLE, c -> c.accumulatedFallDistance() < PARAGLIDING_FALL_DISTANCE && !c.prevState().paragliding());

		register.connect(IDLE, RUNNING, c -> c.player().isSprinting() && !c.player().isUsingItem(), RUNNING_PRIORITY);
		register.connect(IDLE, MIDAIR, c -> !c.player().onGround(), MIDAIR_PRIORITY);
	}

	@Override public void registerStaminaEfficiencyLogic(@NotNull StaminaEfficiencyLogicRegister register) {
		Contents contents = Contents.get();
		register.registerAttribute(contents::staminaEfficiency, (d, c, p) -> d < 0);
		register.registerAttribute(contents::staminaRecovery, (d, c, p) -> d > 0);
		register.registerAttribute(contents::movementStaminaEfficiency, (d, c, p) -> d < 0 && c.state() != null);
		register.registerAttribute(contents::movementStaminaRecovery, (d, c, p) -> d > 0 && c.state() != null);
		register.registerAttribute(contents::paraglidingStaminaEfficiency, (d, c, p) -> d < 0 && c.stateHasFlag(Flags.IS_PARAGLIDING));
		register.registerAttribute(contents::runningStaminaEfficiency, (d, c, p) -> d < 0 && c.stateIs(RUNNING));
		register.registerAttribute(contents::underwaterStaminaEfficiency, (d, c, p) -> d < 0 && c.stateHasFlag(Flags.IS_UNDERWATER));
		register.registerAttribute(contents::swimmingStaminaEfficiency, (d, c, p) -> d < 0 && c.stateIs(SWIMMING));
	}

	@Override public @NotNull ConflictResolver<MovementPlugin, MovementPluginAction> getMovementPluginConflictResolver() {
		return (a, p) -> {
			if (a instanceof NewState) return Resolution.PROCEED; // proceed with initial state registration
			else return Resolution.ABORT; // otherwise abort the change to enable other mods to Do Things
		};
	}
}
