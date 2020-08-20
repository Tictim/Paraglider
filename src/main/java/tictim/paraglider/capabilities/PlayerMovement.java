package tictim.paraglider.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nullable;
import java.util.Objects;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public abstract class PlayerMovement implements ICapabilityProvider{
	@CapabilityInject(PlayerMovement.class)
	public static final Capability<PlayerMovement> CAP = null;

	public static final int BASE_STAMINA = 1000;
	public static final int STAMINA_INCREMENT = BASE_STAMINA/5;
	public static final int RECOVERY_DELAY = 10;
	public static final int MAX_STAMINA_VESSELS = 10;
	public static final int MAX_HEART_CONTAINERS = 20;

	public final PlayerEntity player;
	private PlayerState state = PlayerState.IDLE;

	private int stamina;
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

	protected void updateParagliderInInventory(){
		boolean isParagliding = isParagliding();
		for(int i = 0; i<player.inventory.getSizeInventory(); i++){
			Paraglider cap = player.inventory.getStackInSlot(i).getCapability(Paraglider.CAP).orElse(null);
			if(cap!=null){
				if(i==player.inventory.currentItem){
					if(cap.isParagliding!=isParagliding){
						cap.isParagliding = isParagliding;
						ParagliderUtils.resetMainHandItemEquipProgress();
					}
				}else cap.isParagliding = false;
			}
		}
	}

	protected void applyMovement(){
		if(!player.abilities.isCreativeMode&&isDepleted()){
			player.addPotionEffect(new EffectInstance(Contents.EXHAUSTED.get(), 2, 0, false, false, false));
		}
		if(isParagliding()){
			player.fallDistance = 1.5f;

			Vec3d m = player.getMotion();
			switch(state){
			case PARAGLIDING:
				if(m.y<-0.05) player.setMotion(new Vec3d(m.x, -0.05, m.z));
				break;
			case ASCENDING:
				if(m.y<0.25) player.setMotion(new Vec3d(m.x, Math.max(m.y+0.05, 0.25), m.z));
				break;
			}
		}
	}

	private final LazyOptional<PlayerMovement> self = LazyOptional.of(() -> this);

	@Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side){
		return cap==CAP ? self.cast() : LazyOptional.empty();
	}
}
