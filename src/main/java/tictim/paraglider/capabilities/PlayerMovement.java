package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
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

	public static final int BASE_STAMINA = 1000;
	public static final int STAMINA_INCREMENT = BASE_STAMINA/5;
	public static final int RECOVERY_DELAY = 10;
	public static final int MAX_STAMINA_VESSELS = 10;
	public static final int MAX_HEART_CONTAINERS = 20;

	public final PlayerEntity player;
	private PlayerState state = PlayerState.IDLE;

	private int stamina = BASE_STAMINA;
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
		this.staminaVessels = MathHelper.clamp(staminaVessels, 0, MAX_STAMINA_VESSELS);
	}
	public int getHeartContainers(){
		return heartContainers;
	}
	public void setHeartContainers(int heartContainers){
		this.heartContainers = MathHelper.clamp(heartContainers, 0, MAX_HEART_CONTAINERS);
	}

	public boolean isHeartFullyUpgraded(){
		return heartContainers>=MAX_HEART_CONTAINERS;
	}
	public boolean isStaminaFullyUpgraded(){
		return staminaVessels>=MAX_STAMINA_VESSELS;
	}

	public boolean increaseHeartContainer(){
		if(getHeartContainers()<MAX_HEART_CONTAINERS){
			setHeartContainers(getHeartContainers()+1);
			return true;
		}else return false;
	}
	public boolean decreaseHeartContainer(){
		if(getHeartContainers()>0){
			setHeartContainers(getHeartContainers()-1);
			return true;
		}else return false;
	}

	public boolean increaseStaminaVessel(){
		if(getStaminaVessels()<MAX_STAMINA_VESSELS){
			setStaminaVessels(getStaminaVessels()+1);
			return true;
		}else return false;
	}
	public boolean decreaseStaminaVessel(){
		if(getStaminaVessels()>0){
			setStaminaVessels(getStaminaVessels()-1);
			return true;
		}else return false;
	}

	public int getMaxStamina(){
		return BASE_STAMINA+staminaVessels*STAMINA_INCREMENT;
	}
	public boolean canUseParaglider(){
		return player.abilities.isCreativeMode||!depleted;
	}

	public abstract boolean isParagliding();

	public abstract void update();

	protected void updateStamina(){
		if(state.staminaAction.isConsume){
			recoveryDelay = RECOVERY_DELAY;
			if(!depleted&&(state.isParagliding() ? ModCfg.paraglidingConsumesStamina() : ModCfg.runningConsumesStamina())){
				if(stamina<state.staminaAction.change) stamina = 0;
				else stamina -= state.staminaAction.change;
			}
		}else{
			if(state.staminaAction==PlayerState.StaminaAction.FAST_RECOVER) recoveryDelay = 0;
			if(recoveryDelay>0) recoveryDelay--;
			else if(state.staminaAction.change>0){
				int max = getMaxStamina();
				if(stamina+state.staminaAction.change>=max) stamina = max;
				else stamina += state.staminaAction.change;
			}
		}
	}

	protected void applyMovement(){
		if(!player.abilities.isCreativeMode&&isDepleted()){
			player.addPotionEffect(new EffectInstance(Contents.EXHAUSTED.get(), 2, 0, false, false, false));
		}
		if(isParagliding()){
			player.fallDistance = 1.5f;

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
