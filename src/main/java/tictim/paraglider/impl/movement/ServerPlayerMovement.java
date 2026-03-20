package tictim.paraglider.impl.movement;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.PlayerStateCondition;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogic;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderAdvancements;
import tictim.paraglider.contents.ParagliderTags;
import tictim.paraglider.impl.MovementState;
import tictim.paraglider.impl.SimpleVesselContainer;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.ArrayDeque;

import static tictim.paraglider.impl.movement.PlayerMovementValues.*;

public class ServerPlayerMovement extends PlayerMovement implements PlayerStateCondition.Context {
	private boolean resync;
	private boolean heartContainerChanged = true;
	private boolean staminaVesselChanged = true;
	private boolean movementChanged;

	private double staminaEfficiency;
	private double prevStaminaEfficiency;

	private final ArrayDeque<Effect> particleEffectQueue = new ArrayDeque<>(10);

	private boolean canUseParaglider;
	private boolean canRideUpdraft;
	private boolean paragliding;

	public ServerPlayerMovement(@NotNull ServerPlayer player) {
		super(player);

		SimpleVesselContainer vessels = player.getData(Contents.get().vesselContainer());
		vessels.onChange((actionType, change, playEffect) -> {
			switch (actionType) {
				case HEART_CONTAINER -> this.heartContainerChanged = true;
				case STAMINA_VESSEL -> this.staminaVesselChanged = true;
			}

			if (playEffect) {
				this.particleEffectQueue.add(new Effect(actionType, change));
			}
		});
	}

	@Override public @NotNull ServerPlayer player() {
		return (ServerPlayer)super.player();
	}

	@Override protected @NotNull Stamina createCustomStamina() {
		return ParagliderAPI.staminaFactory().createServerInstance(player());
	}

	@Override protected boolean isRemote() {
		return false;
	}

	@Range(from = 0, to = Integer.MAX_VALUE)
	@Override public int recoveryDelay() {
		return movementState().recoveryDelay();
	}

	@Override public void setRecoveryDelay(int recoveryDelay) {
		movementState().setRecoveryDelay(recoveryDelay);
	}

	@Override public double staminaDelta() {
		return StaminaEfficiencyLogic.applyEfficiency(state().staminaDelta(), this.staminaEfficiency);
	}

	@Override public @NotNull Movement movement() {
		return this;
	}

	@Override public @NotNull PlayerState prevState() {
		return state(); // for PlayerStateCondition the "current" state would be the "previous" state
	}

	@Override public boolean paragliding() {
		return this.paragliding;
	}

	public void setParagliding(boolean paragliding) {
		this.paragliding = paragliding;
	}

	@Override public boolean canDoPanicParagliding() {
		return movementState().canDoPanicParagliding();
	}

	@Override public void update() {
		boolean resync = this.resync;
		this.resync = false;

		boolean vesselsChanged = this.heartContainerChanged || this.staminaVesselChanged;
		if (this.heartContainerChanged) {
			SimpleVesselContainer vessels = player().getData(Contents.get().vesselContainer());

			double delta = ParagliderUtils.refreshAttribute(player(), Attributes.MAX_HEALTH,
					Cfg.get().additionalMaxHealth(vessels.heartContainer()), HEART_CONTAINER_ATTRIBUTE_ID);

			if (delta > 0) {
				player().setHealth(player().getMaxHealth());
			} else {
				float health = player().getHealth();
				float maxHealth = player().getMaxHealth();
				if (health > maxHealth) player().setHealth(maxHealth);
			}
			this.heartContainerChanged = false;
		}

		if (this.staminaVesselChanged) {
			SimpleVesselContainer vessels = player().getData(Contents.get().vesselContainer());

			double delta = ParagliderUtils.refreshAttribute(player(), Contents.get().maxStamina(),
					Cfg.get().maxStamina(vessels.staminaVessel()), STAMINA_VESSEL_ATTRIBUTE_ID);

			if (delta > 0) {
				stamina().setStamina(stamina().maxStamina());
			} else {
				double stamina = stamina().stamina();
				double maxStamina = stamina().maxStamina();
				if (stamina > maxStamina) stamina().setStamina(maxStamina);
			}
			this.staminaVesselChanged = false;
		}

		if (!player().getData(Contents.get().movementInitialized())) {
			player().setData(Contents.get().movementInitialized(), true);
			stamina().setStamina(stamina().maxStamina());
		}

		boolean prevCanUseParaglider = this.canUseParaglider;
		boolean prevCanRideUpdraft = this.canRideUpdraft;
		this.canRideUpdraft = player().isCreative() || !stamina().isDepleted();
		this.canUseParaglider = this.canRideUpdraft || canDoPanicParagliding();

		if (this.paragliding &&
				!(this.canUseParaglider &&
						!player().onGround() &&
						(state().paragliding() || state().hasFlag(ParagliderPlayerStates.Flags.CAN_USE_PARAGLIDER)) &&
						ParagliderUtils.holdingUsableParaglider(player()))) {
			this.paragliding = false;
			ParagliderNetwork.get().setParaglidingToClient(player(), this.paragliding);
		}

		PlayerState prevState = state();
		setState(ParagliderMod.instance().getPlayerConnectionMap()
				.evaluate(ParagliderMod.instance().getLocalPlayerStateMap(), this));

		this.staminaEfficiency = StaminaEfficiencyLogic.handler().getEfficiencySum(state().staminaDelta(), player(), state());
		if (this.prevStaminaEfficiency != this.staminaEfficiency) {
			this.movementChanged = true;
		}

		if (!prevState.equals(state())) {
			this.movementChanged = true;

			if (!prevState.paragliding() && state().paragliding()) {
				ParagliderUtils.playParagliderDeploySound(player());
			}
		}

		updateStamina();

		if (!player().isCreative() && stamina().isDepleted()) {
			ParagliderUtils.addExhaustion(player());
			ParagliderUtils.addFlyingBan(player());
		} else {
			ParagliderUtils.removeExhaustion(player());
			ParagliderUtils.removeFlyingBan(player());
		}

		applyMovement(state().paragliding(), this.canRideUpdraft);

		if (resync || this.movementChanged || stamina().isDirty()) {
			ParagliderNetwork.get().syncMovement(player(),
					state().id(),
					stamina().stamina(),
					stamina().extraStamina(),
					stamina().isDepleted(),
					recoveryDelay(),
					this.staminaEfficiency);
			this.movementChanged = false;
			stamina().setDirty(false);
		}

		if (resync || vesselsChanged) {
			SimpleVesselContainer vessels = player().getData(Contents.get().vesselContainer());

			ParagliderNetwork.get().syncVessels(player(),
					stamina().stamina(),
					stamina().extraStamina(),
					stamina().isDepleted(),
					vessels.heartContainer(),
					vessels.staminaVessel());

			if (vesselsChanged &&
					Cfg.get().maxHeartContainers() <= vessels.heartContainer() &&
					Cfg.get().maxStaminaVessels() <= vessels.staminaVessel()) {
				ParagliderUtils.giveAdvancement(player(), ParagliderAdvancements.ALL_VESSELS, "code_triggered");
			}
		}

		if (resync || prevCanUseParaglider != this.canUseParaglider || prevCanRideUpdraft != this.canRideUpdraft) {
			ParagliderNetwork.get().syncCanUseParaglider(player(), this.canUseParaglider, this.canRideUpdraft);
		}

		this.prevStaminaEfficiency = this.staminaEfficiency;

		for (int i = 0; i < player().getInventory().getContainerSize(); i++) {
			ItemStack stack = player().getInventory().getItem(i);
			if (stack.is(ParagliderTags.PARAGLIDERS)) {
				ParagliderUtils.getCaps(stack).setParagliding(stack,
						i == player().getInventory().getSelectedSlot() && state().paragliding());
			}
		}

		while (!this.particleEffectQueue.isEmpty()) {
			Effect effect = this.particleEffectQueue.pop();
			switch (effect.actionType) {
				case HEART_CONTAINER -> spawnParticle(ParticleTypes.HEART, 5 + 5 * effect.change);
				case STAMINA_VESSEL -> spawnParticle(ParticleTypes.HAPPY_VILLAGER, 7 + 7 * effect.change);
			}
		}
	}

	@Override protected void applyMovement(boolean paragliding, boolean canRideUpdraft) {
		super.applyMovement(paragliding, canRideUpdraft);

		if (paragliding) {
			player().connection.aboveGroundTickCount = 0;
			ItemStack stack = player().getMainHandItem();
			if (stack.is(ParagliderTags.PARAGLIDERS)) {
				ParagliderUtils.getCaps(stack).damageParaglider(player(), stack);
			}
		}

		if (!player().isCreative() && stamina().isDepleted()) {
			MovementState movementState = movementState();
			if (movementState.panicParaglidingDelay() > 0) {
				if (!player().onGround()) {
					movementState.setPanicParaglidingDelay(movementState.panicParaglidingDelay() - 1);
				} else movementState.resetPanicParaglidingState();
			} else if (movementState.panicParagliding()) {
				movementState.setPanicParaglidingDelay(PANIC_DELAY);
				movementState.setPanicParagliding(false);
			} else if (paragliding) { // only active panic paragliding when the user is paragliding
				movementState.setPanicParaglidingDelay(PANIC_DURATION);
				movementState.setPanicParagliding(true);
			}
		}
	}

	@Override protected void updateStamina() {
		boolean remote = isRemote();
		Stamina stamina = stamina();

		if (!stamina.updateWithDefaultLogic(remote)) return;

		boolean wasDepleted = stamina.isDepleted();

		super.updateStamina();

		if (stamina.isDepleted()) {
			if (stamina.stamina() >= Math.min(stamina.maxStamina(), Stamina.STAMINA_PER_WHEEL * 3)) {
				stamina.setDepleted(false, true);
				this.movementChanged = true;
			}
		} else if (stamina.stamina() <= 0 && stamina.extraStamina() <= 0) {
			stamina.setDepleted(true, true);
			movementState().resetPanicParaglidingState();
			this.movementChanged = true;
		}

		if (wasDepleted != stamina.isDepleted()) this.movementChanged = true;
	}

	/**
	 * Causes every information (vessels and movement) to be synced to client on next tick.
	 */
	public void markForSync() {
		this.resync = true;
	}

	protected void spawnParticle(@NotNull ParticleOptions particle, int count) {
		player().level().sendParticles(particle,
				player().getX(), player().getY(.5), player().getZ(),
				count, 1, 2, 1, 0);
	}

	protected MovementState movementState() {
		return player().getData(Contents.get().movementState());
	}

	private record Effect(VesselContainer.ActionType actionType, int change) {}
}
