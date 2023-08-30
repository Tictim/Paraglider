package tictim.paraglider.impl.movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.Serde;
import tictim.paraglider.api.item.Paraglider;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.ParagliderAdvancements;
import tictim.paraglider.impl.vessel.SimpleVesselContainer;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.Objects;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;
import static tictim.paraglider.impl.movement.PlayerMovementValues.*;

public class ServerPlayerMovement extends PlayerMovement implements Serde{
	private boolean heartContainerChanged = true;
	private boolean staminaVesselChanged = true;

	private int panicParaglidingDelay = PANIC_INITIAL_DELAY;
	private boolean panicParagliding = false;

	private double prevY;
	private double accumulatedFallDistance;

	public ServerPlayerMovement(@NotNull ServerPlayer player){
		super(player);
	}

	@Override @NotNull public ServerPlayer player(){
		return (ServerPlayer)super.player();
	}
	@Override @NotNull protected Stamina createStamina(){
		return ParagliderAPI.staminaFactory().createServerInstance(player());
	}
	@Override @NotNull protected VesselContainer createVesselContainer(){
		return new SimpleVesselContainer(player()){
			@Override protected void onChange(@NotNull ActionType actionType, int change){
				switch(actionType){
					case HEART_CONTAINER -> heartContainerChanged = true;
					case STAMINA_VESSEL -> staminaVesselChanged = true;
				}
			}
		};
	}

	@Override public void update(){
		boolean vesselsChanged = this.heartContainerChanged||this.staminaVesselChanged;
		if(this.heartContainerChanged){
			double delta;
			double value = Cfg.get().additionalMaxHealth(vessels().heartContainer());
			AttributeInstance attrib = player().getAttribute(Attributes.MAX_HEALTH);
			if(attrib!=null){
				AttributeModifier prev = attrib.getModifier(HEART_CONTAINER_UUID);
				if(prev!=null) attrib.removeModifier(prev);
				if(value!=0){
					attrib.addPermanentModifier(new AttributeModifier(HEART_CONTAINER_UUID,
							"Heart Containers", value, AttributeModifier.Operation.ADDITION));
				}
				delta = value-(prev!=null ? prev.getAmount() : 0);
			}else delta = 0;

			player().setHealth(Math.min(player().getMaxHealth(), player().getHealth()+Math.max(0, (float)delta)));
			this.heartContainerChanged = false;
		}
		if(this.staminaVesselChanged){
			stamina().setStamina(Math.min(stamina().stamina(), stamina().maxStamina()));
			this.staminaVesselChanged = false;
		}

		if(player().onGround()||player().getY()>this.prevY) this.accumulatedFallDistance = 0;
		else accumulatedFallDistance += this.prevY-player().getY();

		PlayerState prevState = state();
		setState(ParagliderMod.instance().getPlayerConnectionMap()
				.evaluate(ParagliderMod.instance().getPlayerStateMap(),
						player(), state(),
						player().isCreative()||!stamina().isDepleted()||canDoPanicParagliding(),
						this.accumulatedFallDistance));

		boolean movementChanged = !prevState.equals(state());

		boolean wasDepleted = stamina().isDepleted();
		stamina().update(this);
		if(stamina().isDepleted()){
			if(stamina().stamina()>=stamina().maxStamina()){
				stamina().setDepleted(false);
				movementChanged = true;
			}
		}else if(stamina().stamina()<=0){
			stamina().setDepleted(true);
			this.panicParaglidingDelay = PANIC_INITIAL_DELAY;
			this.panicParagliding = false;
			movementChanged = true;
		}
		if(wasDepleted!=stamina().isDepleted()){
			movementChanged = true;
		}

		if(!player().isCreative()&&stamina().isDepleted()){
			ParagliderUtils.addExhaustion(player());
		}else{
			ParagliderUtils.removeExhaustion(player());
		}
		applyMovement();

		if(movementChanged){
			ParagliderNetwork.get().syncMovement(player(),
					state().id(),
					stamina().stamina(),
					stamina().isDepleted(),
					recoveryDelay());
		}

		if(vesselsChanged){
			ParagliderNetwork.get().syncVessels(player(),
					stamina().stamina(),
					vessels().heartContainer(),
					vessels().staminaVessel());

			if(Cfg.get().maxHeartContainers()<=vessels().heartContainer()&&
					Cfg.get().maxStaminaVessels()<=vessels().staminaVessel()){
				ParagliderUtils.giveAdvancement(player(), ParagliderAdvancements.ALL_VESSELS, "code_triggered");
			}
		}

		this.prevY = player().getY();

		for(int i = 0; i<player().getInventory().getContainerSize(); i++){
			ItemStack stack = player().getInventory().getItem(i);
			if(stack.getItem() instanceof Paraglider p){
				p.setParagliding(stack, i==player().getInventory().selected&&state().has(FLAG_PARAGLIDING));
			}
		}
	}

	@Override protected void applyMovement(){
		super.applyMovement();
		if(state().has(FLAG_PARAGLIDING)){
			player().connection.aboveGroundTickCount = 0;
			ItemStack stack = player().getMainHandItem();
			if(stack.getItem() instanceof Paraglider p){
				p.damageParaglider(player(), stack);
			}
			if(!player().isCreative()&&stamina().isDepleted()){
				if(this.panicParaglidingDelay>0){
					this.panicParaglidingDelay--;
				}else{
					this.panicParaglidingDelay = this.panicParagliding ? PANIC_DELAY : PANIC_DURATION;
					this.panicParagliding = !this.panicParagliding;
				}
			}
		}
	}

	/**
	 * "Panic Paragliding" refers to the game mechanic that enables players to use Paraglider for a brief second after
	 * running out of stamina.
	 *
	 * @return Whether you can perform "Panic Paragliding" this tick
	 */
	public boolean canDoPanicParagliding(){
		return this.panicParagliding;
	}

	@Override public void read(@NotNull CompoundTag tag){
		// retro save compat
		if(tag.contains("stamina", CompoundTag.TAG_INT)){
			if(stamina() instanceof Serde serde){
				CompoundTag tag2 = new CompoundTag();
				tag2.putInt("stamina", tag.getInt("stamina"));
				tag2.putBoolean("depleted", tag.getBoolean("depleted"));
				serde.read(tag2);
			}
			if(vessels() instanceof Serde serde){
				CompoundTag tag2 = new CompoundTag();
				tag2.putInt("heartContainers", tag.getInt("heartContainers"));
				tag2.putInt("staminaVessels", tag.getInt("staminaVessels"));
				tag2.putInt("essences", tag.getInt("essence")); // add missing s
				serde.read(tag2);
			}
		}else{
			if(stamina() instanceof Serde serde) serde.read(tag.getCompound("stamina"));
			if(vessels() instanceof Serde serde) serde.read(tag.getCompound("vessels"));
		}
		setRecoveryDelay(tag.getInt("recoveryDelay"));
		this.panicParaglidingDelay = tag.getInt("panicParaglidingDelay");
		this.panicParagliding = tag.getBoolean("panicParagliding");

		this.heartContainerChanged = true;
		this.staminaVesselChanged = true;
	}

	@Override @NotNull public CompoundTag write(){
		CompoundTag tag = new CompoundTag();
		if(stamina() instanceof Serde serde){
			tag.put("stamina", Objects.requireNonNull(serde.write(), stamina()+"#write() returned null!"));
		}
		if(vessels() instanceof Serde serde){
			tag.put("vessels", Objects.requireNonNull(serde.write(), vessels()+"#write() returned null!"));
		}
		tag.putInt("recoveryDelay", recoveryDelay());
		tag.putInt("panicParaglidingDelay", panicParaglidingDelay);
		tag.putBoolean("panicParagliding", panicParagliding);
		return tag;
	}
}
