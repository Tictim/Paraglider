package tictim.paraglider.impl.movement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;

import java.util.Objects;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_ASCENDING;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

public abstract class PlayerMovement implements Movement {
	private final Player player;
	private @Nullable PlayerState state;

	private boolean staminaInitialized;
	private @Nullable Stamina stamina;

	protected double staminaReductionRate;

	public PlayerMovement(@NotNull Player player) {
		this.player = Objects.requireNonNull(player, "player == null");
	}

	public @NotNull Player player() {
		return player;
	}

	public final @NotNull Stamina stamina() {
		if (!this.staminaInitialized) {
			this.stamina = Objects.requireNonNull(createCustomStamina(), "createCustomStamina() returned null");
			this.staminaInitialized = true;
		}
		return Objects.requireNonNull(this.stamina);
	}

	protected abstract @NotNull Stamina createCustomStamina();

	protected abstract boolean isRemote();

	@Override public final @NotNull PlayerState state() {
		if (this.state != null) return this.state;
		PlayerStateMap stateMap = isRemote() ?
				ParagliderMod.instance().getPlayerStateMap() :
				ParagliderMod.instance().getLocalPlayerStateMap();
		return stateMap.getIdleState();
	}

	protected final void setState(@NotNull PlayerState state) {
		this.state = state;
	}

	@Override public double staminaReductionRate() {
		return staminaReductionRate;
	}

	@Override public int getActualStaminaDelta() {
		return ParagliderUtils.applyReductionToDelta(state().staminaDelta(), staminaReductionRate());
	}

	public abstract void update();

	protected void applyMovement() {
		Player player = player();
		PlayerState state = state();

		if (state.has(FLAG_PARAGLIDING)) {
			player.fallDistance = 0;

			Vec3 m = player.getDeltaMovement();
			if (state.has(FLAG_ASCENDING)) {
				if (m.y < 0.25) player.setDeltaMovement(new Vec3(m.x, Math.max(m.y + 0.05, 0.25), m.z));
			} else {
				if (m.y < -0.05) player.setDeltaMovement(new Vec3(m.x, -0.05, m.z));
			}
		}
	}

	protected void updateStamina() {
		boolean remote = isRemote();
		Stamina stamina = stamina();

		if (!stamina.updateWithDefaultLogic(remote)) return;

		PlayerState state = state();
		int recoveryDelay = recoveryDelay();
		int newRecoveryDelay = recoveryDelay;
		int delta = getActualStaminaDelta();

		if (delta < 0) {
			if (!stamina.isDepleted()) stamina.takeStamina(-delta, false, false);
		} else {
			if (recoveryDelay > 0) newRecoveryDelay--;
			else if (delta > 0) stamina.giveStamina(delta, false);
		}

		//noinspection DataFlowIssue
		newRecoveryDelay = Math.max(0, Math.max(newRecoveryDelay, state.recoveryDelay()));
		if (recoveryDelay != newRecoveryDelay) setRecoveryDelay(newRecoveryDelay);
	}
}
