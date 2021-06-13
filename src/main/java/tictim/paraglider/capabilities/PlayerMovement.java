package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class PlayerMovement implements ICapabilityProvider{
	@CapabilityInject(PlayerMovement.class)
	public static Capability<PlayerMovement> CAP = null;

	public static final int RECOVERY_DELAY = 10;

	public final PlayerEntity player;
	private PlayerState state = PlayerState.IDLE;

	private int stamina = ModCfg.startingStamina();
	private boolean depleted;
	private int recoveryDelay;
	private int staminaVessels;
	private int heartContainers;

	public PlayerMovement(PlayerEntity player){
		this.player = Objects.requireNonNull(player);
	}

	public PlayerState getState(){
		return this.state;
	}
	public void setState(PlayerState state){
		this.state = Objects.requireNonNull(state);
	}

	public int getStamina(){
		return stamina;
	}
	public void setStamina(int stamina){
		this.stamina = stamina;
	}
	public boolean isDepleted(){
		return depleted;
	}
	public void setDepleted(boolean depleted){
		this.depleted = depleted;
	}
	public int getRecoveryDelay(){
		return recoveryDelay;
	}
	public void setRecoveryDelay(int recoveryDelay){
		this.recoveryDelay = recoveryDelay;
	}

	public int getStaminaVessels(){
		return staminaVessels;
	}
	public void setStaminaVessels(int staminaVessels){
		this.staminaVessels = Math.max(0, staminaVessels);
	}
	public int getHeartContainers(){
		return heartContainers;
	}
	public void setHeartContainers(int heartContainers){
		this.heartContainers = Math.max(0, heartContainers);
	}

	public int getMaxStamina(){
		return ModCfg.maxStamina(staminaVessels);
	}
	public boolean canUseParaglider(){
		return player.abilities.isCreativeMode||!depleted;
	}

	public abstract boolean isParagliding();

	public abstract void update();

	protected void updateStamina(){
		if(state.isConsume()){
			recoveryDelay = RECOVERY_DELAY;
			if(!depleted&&(state.isParagliding() ? ModCfg.paraglidingConsumesStamina() : ModCfg.runningConsumesStamina()))
				stamina = Math.max(0, stamina+state.change());
		}else if(recoveryDelay>0) recoveryDelay--;
		else if(state.change()>0) stamina = Math.min(getMaxStamina(), stamina+state.change());
	}

	protected void applyMovement(){
		if(!player.abilities.isCreativeMode&&isDepleted()){
			player.addPotionEffect(new EffectInstance(Contents.EXHAUSTED.get(), 2, 0, false, false, false));
		}
		if(isParagliding()){
			player.fallDistance = 0;

			Vector3d m = player.getMotion();
			switch(state){
				case PARAGLIDING:
					if(m.y<-0.05) player.setMotion(new Vector3d(m.x, -0.05, m.z));
					break;
				case ASCENDING:
					if(m.y<0.25) player.setMotion(new Vector3d(m.x, Math.max(m.y+0.05, 0.25), m.z));
					break;
			}
		}
	}

	public void copyTo(PlayerMovement another){
		another.setRecoveryDelay(getRecoveryDelay());
		another.setStaminaVessels(getStaminaVessels());
		another.setHeartContainers(getHeartContainers());
		another.setStamina(getMaxStamina());
	}

	private final LazyOptional<PlayerMovement> self = LazyOptional.of(() -> this);

	@Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==CAP ? self.cast() : LazyOptional.empty();
	}

	@SuppressWarnings("ConstantConditions")
	@Nullable public static PlayerMovement of(ICapabilityProvider capabilityProvider){
		return capabilityProvider.getCapability(CAP).orElse(null);
	}
}
