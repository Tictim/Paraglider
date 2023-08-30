package tictim.paraglider.plugin;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.item.Paraglider;
import tictim.paraglider.api.movement.MovementPlugin;
import tictim.paraglider.api.movement.MovementPluginAction;
import tictim.paraglider.api.movement.MovementPluginAction.NewState;
import tictim.paraglider.api.plugin.ConflictResolver;
import tictim.paraglider.api.plugin.ConflictResolver.Resolution;
import tictim.paraglider.api.plugin.ParagliderPlugin;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.wind.Wind;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.*;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.*;
import static tictim.paraglider.impl.movement.PlayerMovementValues.PARAGLIDING_FALL_DISTANCE;

@ParagliderPlugin
public class ParagliderDefaultPlugin implements MovementPlugin{
	@Override public void registerNewStates(@NotNull PlayerStateRegister register){
		register.register(IDLE, IDLE_STAMINA_DELTA);
		register.register(FLYING, FLYING_STAMINA_DELTA);
		register.register(ON_VEHICLE, ON_VEHICLE_STAMINA_DELTA);
		register.register(SWIMMING, SWIMMING_STAMINA_DELTA, FLAG_RUNNING);
		register.register(UNDERWATER, UNDERWATER_STAMINA_DELTA, FLAG_RUNNING);
		register.register(BREATHING_UNDERWATER, BREATHING_UNDERWATER_STAMINA_DELTA, FLAG_RUNNING);
		register.register(PARAGLIDING, PARAGLIDING_STAMINA_DELTA, FLAG_PARAGLIDING);
		register.register(PANIC_PARAGLIDING, PANIC_PARAGLIDING_STAMINA_DELTA, FLAG_PARAGLIDING);
		register.register(ASCENDING, ASCENDING_STAMINA_DELTA, FLAG_PARAGLIDING, FLAG_ASCENDING);
		register.register(RUNNING, RUNNING_STAMINA_DELTA, FLAG_RUNNING);
		register.register(MIDAIR, MIDAIR_STAMINA_DELTA);
	}

	@Override public void registerStateConnections(@NotNull PlayerStateConnectionRegister register){
		register.addBranch(IDLE, (p, s, b, f) -> p.getAbilities().flying||p.isFallFlying(), FLYING, FLYING_PRIORITY);
		register.addBranch(IDLE, (p, s, b, f) -> p.getVehicle()!=null, ON_VEHICLE, ON_VEHICLE_PRIORITY);
		register.addBranch(IDLE, (p, s, b, f) -> p.isSwimming(), SWIMMING, SWIMMING_PRIORITY);
		register.addBranch(IDLE, (p, s, b, f) -> p.isInWater(), UNDERWATER, UNDERWATER_PRIORITY);
		register.addBranch(UNDERWATER,
				(p, s, b, f) -> ParagliderUtils.canBreatheUnderwater(p),
				BREATHING_UNDERWATER);

		register.addBranch(IDLE,
				(p, s, b, f) -> b&&
						!p.onGround()&&
						!p.isFallFlying()&&
						p.getMainHandItem().getItem() instanceof Paraglider item&&
						item.canDoParagliding(p.getMainHandItem()),
				PARAGLIDING, PARAGLIDING_PRIORITY);

		register.addBranch(PARAGLIDING, (p, s, b, f) -> f>=PARAGLIDING_FALL_DISTANCE&&!p.isCreative()&&Stamina.get(p).isDepleted(), PANIC_PARAGLIDING);
		register.addBranch(PARAGLIDING,
				(p, s, b, f) -> Cfg.get().ascendingWinds()&&Wind.isInside(p.level(), p.getBoundingBox()),
				ASCENDING);
		register.addBranch(PARAGLIDING, (p, s, b, f) -> f<PARAGLIDING_FALL_DISTANCE&&!s.has(FLAG_PARAGLIDING), IDLE);

		register.addBranch(IDLE, (p, s, b, f) -> p.isSprinting()&&!p.isUsingItem(), RUNNING, RUNNING_PRIORITY);
		register.addBranch(IDLE, (p, s, b, f) -> !p.onGround(), MIDAIR, MIDAIR_PRIORITY);
	}

	private static final ConflictResolver<MovementPlugin, MovementPluginAction> RESOLVER = (a, p) -> {
		if(a instanceof NewState) return Resolution.PROCEED; // proceed with initial state registration
		else return Resolution.ABORT; // otherwise abort the change to enable other mods to Do Things
	};

	@Override @NotNull public ConflictResolver<MovementPlugin, MovementPluginAction> getMovementPluginConflictResolver(){
		return RESOLVER;
	}
}
