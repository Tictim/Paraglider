package tictim.paraglider.impl.movement;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.wind.WindLogic;

import java.util.Objects;

@NullMarked
public abstract class PlayerMovement implements Movement {
	private final Player player;
	private @Nullable PlayerState state;

	private boolean staminaInitialized;
	private @Nullable Stamina stamina;

	public PlayerMovement(Player player) {
		this.player = Objects.requireNonNull(player, "player == null");
	}

	public Player player() {
		return player;
	}

	public final Stamina stamina() {
		if (!this.staminaInitialized) {
			this.stamina = Objects.requireNonNull(createCustomStamina(), "createCustomStamina() returned null");
			this.staminaInitialized = true;
		}
		return Objects.requireNonNull(this.stamina);
	}

	protected abstract Stamina createCustomStamina();

	protected abstract boolean isRemote();

	@Override public final PlayerState state() {
		if (this.state != null) return this.state;
		PlayerStateMap stateMap = isRemote() ?
				ParagliderMod.instance().getPlayerStateMap() :
				ParagliderMod.instance().getLocalPlayerStateMap();
		return stateMap.getIdleState();
	}

	protected final void setState(PlayerState state) {
		this.state = state;
	}

	public abstract void update();

	protected void applyMovement(boolean paragliding, boolean canRideUpdraft) {
		if (!paragliding) return;

		Player player = player();
		Vec3 m = player.getDeltaMovement();
		double wind = canRideUpdraft && Cfg.get().updraft() ? WindLogic.getWindAbove(player.level(), player.getBoundingBox()) : 0.0;
		double dy;

		if (wind > 0.0) {
			// larger wind above = stronger updraft force
			dy = Math.max(0, Mth.lerp(Math.min(wind, 2.0) / 2.0, -0.05, 0.25));
		} else {
			dy = -0.05;
		}

		player.fallDistance = 0;
		player.setDeltaMovement(m.x, Math.max(m.y, dy), m.z);
	}

	protected void updateStamina() {
		boolean remote = isRemote();
		Stamina stamina = stamina();

		if (!stamina.updateWithDefaultLogic(remote)) return;

		PlayerState state = state();
		int recoveryDelay = recoveryDelay();
		int newRecoveryDelay = recoveryDelay;
		double delta = staminaDelta();

		if (delta < 0) {
			if (!stamina.isDepleted()) stamina.takeStamina(-delta, false, false, true);
		} else {
			if (recoveryDelay > 0) newRecoveryDelay--;
			else if (delta > 0) stamina.giveStamina(delta, false, true);
		}

		//noinspection DataFlowIssue
		newRecoveryDelay = Math.max(0, Math.max(newRecoveryDelay, state.recoveryDelay()));
		if (recoveryDelay != newRecoveryDelay) setRecoveryDelay(newRecoveryDelay);
	}
}
