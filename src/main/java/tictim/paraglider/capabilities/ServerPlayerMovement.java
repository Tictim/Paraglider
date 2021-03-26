package tictim.paraglider.capabilities;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncMovementMsg;
import tictim.paraglider.network.SyncParaglidingMsg;
import tictim.paraglider.network.SyncVesselMsg;
import tictim.paraglider.utils.WindUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ServerPlayerMovement extends PlayerMovement implements INBTSerializable<CompoundNBT>{
	public static final int PANIC_INITIAL_DELAY = 10;
	public static final int PANIC_DELAY = 30;
	public static final int PANIC_DURATION = 15;
	public static final UUID HEART_CONTAINER_UUID = UUID.fromString("a0f1c25b-c4f9-4413-9619-7841cd7982a3");

	private final ServerPlayerEntity serverPlayer;

	private PlayerState prevState = PlayerState.IDLE;

	private boolean healthNeedsUpdate;
	private boolean prevIsParagliding;

	public boolean vesselNeedsSync;
	public boolean movementNeedsSync;
	public boolean paraglidingNeedsSync;

	private int panicParaglidingDelay = PANIC_INITIAL_DELAY;
	private int panicParaglidingDuration = 0;

	private int essenceSoldToStatue;

	public ServerPlayerMovement(ServerPlayerEntity player){
		super(player);
		serverPlayer = player;
	}

	public int getEssenceSoldToStatue(){
		return essenceSoldToStatue;
	}
	public void setEssenceSoldToStatue(int essenceSoldToStatue){
		this.essenceSoldToStatue = Math.max(essenceSoldToStatue, 0);
	}

	public boolean increaseEssenceSoldToStatue(){
		if(essenceSoldToStatue==Integer.MAX_VALUE) return false;
		essenceSoldToStatue++;
		return true;
	}

	public boolean decreaseEssenceSoldToStatue(){
		if(essenceSoldToStatue==0) return false;
		essenceSoldToStatue--;
		return true;
	}

	@Override public void setStaminaVessels(int staminaVessels){
		int cache = this.getStaminaVessels();
		super.setStaminaVessels(staminaVessels);
		if(cache!=staminaVessels) vesselNeedsSync = true;

	}
	@Override public void setHeartContainers(int heartContainers){
		int cache = this.getHeartContainers();
		super.setHeartContainers(heartContainers);
		if(cache!=heartContainers) vesselNeedsSync = true;
		healthNeedsUpdate = true;
	}

	@Override public boolean isParagliding(){
		return prevIsParagliding;
	}

	@Override public void update(){
		if(healthNeedsUpdate){
			ModifiableAttributeInstance attrib = player.getAttribute(Attributes.MAX_HEALTH);
			if(attrib!=null){
				attrib.removeModifier(HEART_CONTAINER_UUID);
				if(getHeartContainers()>0)
					attrib.applyNonPersistentModifier(new AttributeModifier(HEART_CONTAINER_UUID, () -> "Heart Containers", getHeartContainers()*2, AttributeModifier.Operation.ADDITION));
				double mhp = attrib.getValue();
				if(player.getHealth()>mhp) player.setHealth((float)mhp);
			}
			healthNeedsUpdate = false;
		}

		boolean isHoldingParaglider = Paraglider.isParaglider(player.getHeldItemMainhand());
		setState(calculatePlayerState(isHoldingParaglider));
		if(prevState!=getState()) movementNeedsSync = true;
		updateStamina();

		boolean isParagliding = getState().isParagliding()&&(canUseParaglider()||tryPanicParagliding());
		if(prevIsParagliding!=isParagliding){
			paraglidingNeedsSync = true;
			prevIsParagliding = isParagliding;
		}
		if(!player.abilities.isCreativeMode&&isDepleted()){
			player.addPotionEffect(new EffectInstance(Contents.EXHAUSTED.get(), 2, 0, false, false, false));
		}
		applyMovement();

		if(movementNeedsSync){
			SyncMovementMsg msg = new SyncMovementMsg(this);
			if(ModCfg.traceMovementPacket()) ParagliderMod.LOGGER.debug("Sending packet {} to player {}", msg, player);
			ModNet.NET.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
			paraglidingNeedsSync = true;
			movementNeedsSync = false;
		}
		if(paraglidingNeedsSync){
			SyncParaglidingMsg msg2 = new SyncParaglidingMsg(this);
			if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Sending packet {} to player tracking {}", msg2, player);
			ModNet.NET.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), msg2);
			paraglidingNeedsSync = false;
		}
		if(vesselNeedsSync){
			SyncVesselMsg msg = new SyncVesselMsg(getStamina(), getHeartContainers(), getStaminaVessels());
			if(ModCfg.traceVesselPacket()) ParagliderMod.LOGGER.debug("Sending packet {} to player {}", msg, player);
			ModNet.NET.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
			vesselNeedsSync = false;
		}

		prevState = getState();
	}

	private PlayerState calculatePlayerState(boolean isHoldingParaglider){
		if(player.abilities.isFlying) return PlayerState.IDLE;
		else if(player.getRidingEntity()!=null) return PlayerState.RIDING;
		else if(player.isSwimming()) return PlayerState.SWIMMING;
		else if(player.isInWater()) return canSwimInfinitely() ? PlayerState.BREATHING_UNDERWATER : PlayerState.UNDERWATER;
		else if(!player.isOnGround()&&isHoldingParaglider&&!player.isElytraFlying()){
			if(ModCfg.ascendingWinds()&&WindUtils.isInsideWind(player.world, player.getBoundingBox())) return PlayerState.ASCENDING;
			else if(prevState.isParagliding()||player.fallDistance>=1.45f) return PlayerState.PARAGLIDING;
		}

		if(player.isSprinting()&&!player.isHandActive()) return PlayerState.RUNNING;
		else if(player.isOnGround()) return PlayerState.IDLE;
		else return PlayerState.MIDAIR;
	}

	private boolean canSwimInfinitely(){
		if(player.isPotionActive(Effects.WATER_BREATHING)) return true;

		ItemStack head = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
		if(!head.isEmpty()){
			if(head.getItem()==Items.TURTLE_HELMET) return true;
			else if(EnchantmentHelper.getEnchantmentLevel(Enchantments.AQUA_AFFINITY, head)>0) return true;
		}
		ItemStack feet = player.getItemStackFromSlot(EquipmentSlotType.FEET);
		if(!feet.isEmpty()){
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, feet)>0) return true;
		}
		return false;
	}

	@Override protected void updateStamina(){
		boolean wasDepleted = isDepleted();
		super.updateStamina();
		if(isDepleted()!=wasDepleted) movementNeedsSync = true;

		if(isDepleted()){
			if(getStamina()>=getMaxStamina()){
				setDepleted(false);
				movementNeedsSync = true;
			}
		}else if(getStamina()<=0){
			setDepleted(true);
			panicParaglidingDelay = PANIC_INITIAL_DELAY;
			movementNeedsSync = true;
		}
	}

	private boolean tryPanicParagliding(){
		if(panicParaglidingDuration>0){
			panicParaglidingDuration--;
			return true;
		}else if(panicParaglidingDelay>0){
			panicParaglidingDelay--;
			return false;
		}else{
			panicParaglidingDelay = PANIC_DELAY;
			panicParaglidingDuration = PANIC_DURATION;
			return true;
		}
	}

	@Override public void copyTo(PlayerMovement another){
		super.copyTo(another);
		if(another instanceof ServerPlayerMovement){
			((ServerPlayerMovement)another).setEssenceSoldToStatue(getEssenceSoldToStatue());
		}
	}

	@Override public CompoundNBT serializeNBT(){
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("stamina", getStamina());
		nbt.putBoolean("depleted", isDepleted());
		nbt.putInt("recoveryDelay", getRecoveryDelay());
		nbt.putInt("panicParaglidingDelay", panicParaglidingDelay);
		nbt.putInt("staminaVessels", getStaminaVessels());
		nbt.putInt("heartContainers", getHeartContainers());
		nbt.putInt("essenceSoldToStatue", getEssenceSoldToStatue());
		return nbt;
	}
	@Override public void deserializeNBT(CompoundNBT nbt){
		setStamina(nbt.getInt("stamina"));
		setDepleted(nbt.getBoolean("depleted"));
		setRecoveryDelay(nbt.getInt("recoveryDelay"));
		panicParaglidingDelay = nbt.getInt("panicParaglidingDelay");
		setStaminaVessels(nbt.getInt("staminaVessels"));
		setHeartContainers(nbt.getInt("heartContainers"));
		setEssenceSoldToStatue(nbt.getInt("essenceSoldToStatue"));
	}

	@Nullable public static ServerPlayerMovement of(ICapabilityProvider capabilityProvider){
		PlayerMovement movement = PlayerMovement.of(capabilityProvider);
		return movement instanceof ServerPlayerMovement ? (ServerPlayerMovement)movement : null;
	}
}
