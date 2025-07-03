package tictim.paraglider.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Range;

import static tictim.paraglider.impl.movement.PlayerMovementValues.PANIC_INITIAL_DELAY;

public class MovementState {
	public static final Codec<MovementState> CODEC = RecordCodecBuilder.create(b -> b.group(
			Codec.INT.fieldOf("recoveryDelay").forGetter(MovementState::recoveryDelay),
			Codec.INT.fieldOf("panicParaglidingDelay").forGetter(MovementState::panicParaglidingDelay),
			Codec.BOOL.fieldOf("panicParagliding").forGetter(MovementState::panicParagliding)
	).apply(b, MovementState::new));

	private int recoveryDelay;

	/**
	 * Panic paragliding delay when {@code panicParagliding == false}, panic paragliding duration when
	 * {@code panicParagliding == true}
	 */
	private int panicParaglidingDelay = PANIC_INITIAL_DELAY;

	/**
	 * {@code false} means panic paragliding mode is recharging. {@code true} means panic paragliding mode is currently
	 * active.
	 */
	private boolean panicParagliding = false;

	public MovementState() {}
	public MovementState(int recoveryDelay, int panicParaglidingDelay, boolean panicParagliding) {
		this.recoveryDelay = Math.max(0, recoveryDelay);
		this.panicParaglidingDelay = panicParaglidingDelay;
		this.panicParagliding = panicParagliding;
	}

	@Range(from = 0, to = Integer.MAX_VALUE)
	public final int recoveryDelay() {
		return recoveryDelay;
	}
	public final void setRecoveryDelay(int recoveryDelay) {
		this.recoveryDelay = Math.max(0, recoveryDelay);
	}

	public int panicParaglidingDelay() {
		return panicParaglidingDelay;
	}
	public void setPanicParaglidingDelay(int panicParaglidingDelay) {
		this.panicParaglidingDelay = panicParaglidingDelay;
	}

	public boolean panicParagliding() {
		return panicParagliding;
	}
	public void setPanicParagliding(boolean panicParagliding) {
		this.panicParagliding = panicParagliding;
	}

	/**
	 * "Panic Paragliding" refers to the game mechanic that enables players to use Paraglider for a brief second after
	 * running out of stamina.
	 *
	 * @return Whether you can perform "Panic Paragliding" this tick
	 */
	public boolean canDoPanicParagliding() {
		return this.panicParagliding || this.panicParaglidingDelay <= 0;
	}

	public void resetPanicParaglidingState() {
		this.panicParaglidingDelay = PANIC_INITIAL_DELAY;
		this.panicParagliding = false;
	}
}
