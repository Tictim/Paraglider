package tictim.paraglider.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModAdvancements;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncMovementMsg;
import tictim.paraglider.network.SyncParaglidingMsg;
import tictim.paraglider.network.SyncVesselMsg;
import tictim.paraglider.wind.Wind;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ServerPlayerMovement extends PlayerMovement implements INBTSerializable<CompoundTag>{
	public static final int PANIC_INITIAL_DELAY = 10;
	public static final int PANIC_DELAY = 30;
	public static final int PANIC_DURATION = 15;
	public static final UUID HEART_CONTAINER_UUID = UUID.fromString("a0f1c25b-c4f9-4413-9619-7841cd7982a3");
	public static final UUID STAMINA_CONTAINER_UUID = UUID.fromString("8eb77123-6306-4188-9227-56082ba4887a");

	private final ServerPlayer serverPlayer;

	private PlayerState prevState = PlayerState.IDLE;

	private boolean healthNeedsUpdate = true;
	private boolean staminaNeedsUpdate = true;
	private boolean prevIsParagliding;

	public boolean vesselNeedsSync;
	public boolean movementNeedsSync;
	public boolean paraglidingNeedsSync;

	private double prevY;
	private double accumulatedFallDistance;

	private int panicParaglidingDelay = PANIC_INITIAL_DELAY;
	private int panicParaglidingDuration = 0;

	private int essence;

	public ServerPlayerMovement(ServerPlayer player){
		super(player);
		serverPlayer = player;
	}

	public int getEssence(){
		return essence;
	}
	public void setEssence(int essence){
		this.essence = Math.max(essence, 0);
	}

	@Override public void setStaminaVessels(int staminaVessels){
		int cache = this.getStaminaVessels();
		super.setStaminaVessels(staminaVessels);
		if(cache!=staminaVessels){
			vesselNeedsSync = true;
			staminaNeedsUpdate = true;
		}
	}
	@Override public void setHeartContainers(int heartContainers){
		int cache = this.getHeartContainers();
		super.setHeartContainers(heartContainers);
		if(cache!=heartContainers){
			vesselNeedsSync = true;
			healthNeedsUpdate = true;
		}
	}

	@Override public boolean isParagliding(){
		return prevIsParagliding;
	}

	@Override public void update(){
		if(healthNeedsUpdate){
			double delta = applyAttribute(Attributes.MAX_HEALTH, HEART_CONTAINER_UUID, "Heart Containers", ModCfg.additionalMaxHealth(getHeartContainers()));
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth()+Math.max(0, (float)delta)));
			healthNeedsUpdate = false;
		}
		if(staminaNeedsUpdate){
			applyAttribute(Contents.MAX_STAMINA.get(), STAMINA_CONTAINER_UUID, "Stamina Vessels", ModCfg.maxStamina(getStaminaVessels()));
			setStamina(Math.min(getStamina(), getMaxStamina()));
			staminaNeedsUpdate = false;
		}

		if(player.isOnGround()||player.getY()>prevY) accumulatedFallDistance = 0;
		else accumulatedFallDistance += prevY-player.getY();

		boolean isHoldingParaglider = Paraglider.isParaglider(player.getMainHandItem());
		setState(calculatePlayerState(isHoldingParaglider));
		if(prevState!=getState()) movementNeedsSync = true;
		updateStamina();

		boolean isParagliding = getState().isParagliding()&&(canUseParaglider()||tryPanicParagliding());
		if(prevIsParagliding!=isParagliding){
			paraglidingNeedsSync = true;
			prevIsParagliding = isParagliding;
		}
		if(!player.isCreative()&&isDepleted()){
			player.addEffect(new MobEffectInstance(Contents.EXHAUSTED.get(), 2, 0, false, false, false));
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

			if(ModCfg.maxHeartContainers()<=getHeartContainers()&&ModCfg.maxStaminaVessels()<=getStaminaVessels()){
				ModAdvancements.give(serverPlayer, ModAdvancements.ALL_VESSELS, "code_triggered");
			}

			vesselNeedsSync = false;
		}

		prevState = getState();
		prevY = player.getY();

		for(int i = 0; i<player.getInventory().getContainerSize(); i++){
			ItemStack stack = player.getInventory().getItem(i);
			if(stack.getItem() instanceof ParagliderItem){ // TODO improve?
				ParagliderItem.setItemParagliding(stack, i==player.getInventory().selected&&isParagliding);
			}
		}
	}

	/**
	 * @return Amount of change in stat
	 */
	private double applyAttribute(Attribute attribute, UUID uuid, String name, double value){
		AttributeInstance attrib = player.getAttribute(attribute);
		if(attrib==null) return 0;
		AttributeModifier prev = attrib.getModifier(uuid);
		if(prev!=null) attrib.removeModifier(prev);
		if(value!=0)
			attrib.addPermanentModifier(new AttributeModifier(
					uuid,
					() -> name,
					value,
					AttributeModifier.Operation.ADDITION));
		return value-(prev!=null ? prev.getAmount() : 0);
	}

	private PlayerState calculatePlayerState(boolean isHoldingParaglider){
		if(player.getAbilities().flying) return PlayerState.IDLE;
		else if(player.getVehicle()!=null) return PlayerState.RIDING;
		else if(player.isSwimming()) return PlayerState.SWIMMING;
		else if(player.isInWater()) return canBreathe() ? PlayerState.BREATHING_UNDERWATER : PlayerState.UNDERWATER;
		else if(!player.isOnGround()&&isHoldingParaglider&&!player.isFallFlying()){
			if(ModCfg.ascendingWinds()&&Wind.isInside(player.level, player.getBoundingBox())) return PlayerState.ASCENDING;
			else if(prevState.isParagliding()||accumulatedFallDistance>=1.45f) return PlayerState.PARAGLIDING;
		}

		if(player.isSprinting()&&!player.isUsingItem()) return PlayerState.RUNNING;
		else if(player.isOnGround()) return PlayerState.IDLE;
		else return PlayerState.MIDAIR;
	}

	private boolean canBreathe(){
		if(player.hasEffect(MobEffects.WATER_BREATHING)) return true;
		if(player.isOnGround()&&(
				!player.canDrownInFluidType(player.getEyeInFluidType())||
						player.level.getBlockState(new BlockPos(player.getX(), player.getEyeY(), player.getZ())).is(Blocks.BUBBLE_COLUMN))){
			return true;
		}

		ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
		if(!head.isEmpty()){
			if(head.getItem()==Items.TURTLE_HELMET) return true;
			else if(head.getEnchantmentLevel(Enchantments.AQUA_AFFINITY)>0) return true;
		}
		ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
		return !feet.isEmpty()&&feet.getEnchantmentLevel(Enchantments.DEPTH_STRIDER)>0;
	}

	@Override protected void applyMovement(){
		super.applyMovement();
		if(isParagliding()){
			serverPlayer.connection.aboveGroundTickCount = 0;
			ItemStack stack = player.getMainHandItem();
			if(Paraglider.isParaglider(stack)){
				damageParagliderWithoutBreaking(player, stack);
			}
		}
	}

	/**
	 * Fuck you, seriously
	 */
	private static void damageParagliderWithoutBreaking(Player player, ItemStack stack){
		AtomicBoolean fuck = new AtomicBoolean();
		int count = stack.getCount();
		stack.hurtAndBreak(1, player, p -> {
			p.broadcastBreakEvent(InteractionHand.MAIN_HAND);
			stack.setCount(count+1);
			fuck.set(true);
		});
		if(fuck.get()){
			stack.setCount(count);
			stack.setDamageValue(stack.getMaxDamage());
		}
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
			((ServerPlayerMovement)another).setEssence(getEssence());
		}
	}

	@Override public CompoundTag serializeNBT(){
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("stamina", getStamina());
		nbt.putBoolean("depleted", isDepleted());
		nbt.putInt("recoveryDelay", getRecoveryDelay());
		nbt.putInt("panicParaglidingDelay", panicParaglidingDelay);
		nbt.putInt("staminaVessels", getStaminaVessels());
		nbt.putInt("heartContainers", getHeartContainers());
		nbt.putInt("essence", getEssence());
		return nbt;
	}
	@Override public void deserializeNBT(CompoundTag nbt){
		setStamina(nbt.getInt("stamina"));
		setDepleted(nbt.getBoolean("depleted"));
		setRecoveryDelay(nbt.getInt("recoveryDelay"));
		panicParaglidingDelay = nbt.getInt("panicParaglidingDelay");
		setStaminaVessels(nbt.getInt("staminaVessels"));
		setHeartContainers(nbt.getInt("heartContainers"));
		setEssence(nbt.getInt("essence"));
	}

	@Nullable public static ServerPlayerMovement of(ICapabilityProvider capabilityProvider){
		PlayerMovement movement = PlayerMovement.of(capabilityProvider);
		return movement instanceof ServerPlayerMovement ? (ServerPlayerMovement)movement : null;
	}
}
