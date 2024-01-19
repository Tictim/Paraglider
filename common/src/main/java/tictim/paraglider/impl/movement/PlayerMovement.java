package tictim.paraglider.impl.movement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.Copy;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;

import java.util.Objects;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_ASCENDING;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

public abstract class PlayerMovement implements Movement, Copy{
	private final Player player;

	@Nullable private Stamina stamina;
	@Nullable private VesselContainer vesselContainer;

	@Nullable private PlayerState state;

	private int recoveryDelay;
	protected double staminaReductionRate;

	public PlayerMovement(@NotNull Player player){
		this.player = Objects.requireNonNull(player, "player == null");
	}

	@NotNull public Player player(){
		return player;
	}

	@NotNull public final Stamina stamina(){
		if(this.stamina!=null) return this.stamina;
		return this.stamina = Objects.requireNonNull(createStamina(), "createStamina() == null");
	}

	@NotNull public final VesselContainer vessels(){
		if(this.vesselContainer!=null) return this.vesselContainer;
		return this.vesselContainer = Objects.requireNonNull(createVesselContainer(), "createVesselContainer() == null");
	}

	@NotNull protected abstract Stamina createStamina();
	@NotNull protected abstract VesselContainer createVesselContainer();

	@Override @NotNull public final PlayerState state(){
		if(state!=null) return state;
		PlayerStateMap stateMap = player().level().isClientSide ?
				ParagliderMod.instance().getPlayerStateMap() :
				ParagliderMod.instance().getLocalPlayerStateMap();
		return stateMap.getIdleState();
	}

	protected final void setState(@NotNull PlayerState state){
		this.state = state;
	}

	@Override @Range(from = 0, to = Integer.MAX_VALUE) public final int recoveryDelay(){
		return recoveryDelay;
	}
	@Override public final void setRecoveryDelay(int recoveryDelay){
		this.recoveryDelay = Math.max(0, recoveryDelay);
	}

	@Override public double staminaReductionRate(){
		return staminaReductionRate;
	}

	@Override public int getActualStaminaDelta(){
		return ParagliderUtils.applyReductionToDelta(state().staminaDelta(), staminaReductionRate());
	}

	public abstract void update();

	@Override public void copyFrom(@NotNull Object from){
		if(!(from instanceof Movement movement)) return;
		setRecoveryDelay(movement.recoveryDelay());
		if(!(from instanceof PlayerMovement playerMovement)) return;
		if(stamina() instanceof Copy copy) copy.copyFrom(playerMovement.stamina());
		if(vessels() instanceof Copy copy) copy.copyFrom(playerMovement.vessels());
	}

	protected void applyMovement(){
		PlayerState state = state();
		if(state.has(FLAG_PARAGLIDING)){
			player().fallDistance = 0;

			Vec3 m = player().getDeltaMovement();
			if(state.has(FLAG_ASCENDING)){
				if(m.y<0.25) player.setDeltaMovement(new Vec3(m.x, Math.max(m.y+0.05, 0.25), m.z));
			}else{
				if(m.y<-0.05) player.setDeltaMovement(new Vec3(m.x, -0.05, m.z));
			}
		}
	}
}
