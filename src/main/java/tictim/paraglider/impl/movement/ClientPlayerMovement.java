package tictim.paraglider.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.ParagliderPlayerStates;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.client.settings.ParagliderClientSettings;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.network.SyncCanUseParagliderHandle;
import tictim.paraglider.network.SyncClientParaglidingHandle;
import tictim.paraglider.wind.WindLogic;

import static tictim.paraglider.impl.movement.PlayerMovementValues.AUTO_PARAGLIDING_FALL_DISTANCE;

public class ClientPlayerMovement extends RemotePlayerMovement implements SyncClientParaglidingHandle, SyncCanUseParagliderHandle {
	private boolean wasParagliding;
	private boolean canUseParaglider;
	private boolean canRideUpdraft;

	/**
	 * Previous Y position for tracking {@link #accumulatedFallDistance}.
	 */
	private double prevY = -1024;

	/**
	 * Self-explanatory. Needs to track it ourselves since fall distance in entity instance often gets overwritten by
	 * other mods and breaks fall distance check.
	 */
	private double accumulatedFallDistance;

	private boolean clientParagliding;
	private boolean autoParagliding;

	public ClientPlayerMovement(@NotNull LocalPlayer player) {
		super(player);
	}

	@Override public @NotNull LocalPlayer player() {
		return (LocalPlayer)super.player();
	}

	public boolean canUseParaglider() {
		return canUseParaglider;
	}

	public boolean clientParagliding() {
		return clientParagliding;
	}

	public boolean autoParagliding() {
		return autoParagliding;
	}

	public void setAutoParagliding(boolean autoParagliding) {
		this.autoParagliding = autoParagliding;
	}

	@Override protected @NotNull Stamina createCustomStamina() {
		return ParagliderAPI.staminaFactory().clientFactory().createLocalClientInstance(player());
	}

	@Override public void update() {
		boolean onGround = player().onGround();
		if (onGround || player().getY() > this.prevY) this.accumulatedFallDistance = 0;
		else accumulatedFallDistance += this.prevY - player().getY();

		if (onGround) this.autoParagliding = true;

		if (this.autoParagliding &&
				ParagliderClientSettings.get().autoParagliding() &&
				!this.clientParagliding &&
				(this.accumulatedFallDistance >= AUTO_PARAGLIDING_FALL_DISTANCE ||
						WindLogic.getWindAbove(player().level(), player().getBoundingBox()) > 0.0)) {
			useParaglider();
			if (this.clientParagliding) {
				ParagliderUtils.applyParagliderItemCooldown(player());
				ParagliderNetwork.get().applyParagliderItemCooldown();
			}
		}
		if (this.clientParagliding && !ParagliderUtils.holdingUsableParaglider(player())) {
			stopUsingParaglider();
		}

		updateStamina();

		boolean paragliding = state().paragliding();
		if (!player().isCreative() && stamina().isDepleted()) {
			player().setSprinting(false);
			player().setSwimming(false);
			if (player().isFallFlying()) player().stopFallFlying();
		} else if (this.wasParagliding != paragliding) {
			player().setSprinting(paragliding);
			if (!this.wasParagliding && paragliding) {
				ParagliderUtils.playParagliderDeploySound(player());
			}
		}

		applyMovement(this.clientParagliding, this.canRideUpdraft);

		this.wasParagliding = paragliding;
		this.prevY = player().getY();
	}

	public void useParaglider() {
		if (!this.clientParagliding &&
				!state().paragliding() &&
				state().hasFlag(ParagliderPlayerStates.Flags.CAN_USE_PARAGLIDER) &&
				this.canUseParaglider &&
				!player().onGround() &&
				ParagliderUtils.holdingUsableParaglider(player())) {
			this.clientParagliding = true;
			ParagliderNetwork.get().setParaglidingToServer(true);
		}
	}

	public void stopUsingParaglider() {
		if (this.clientParagliding) {
			this.clientParagliding = false;
			ParagliderNetwork.get().setParaglidingToServer(false);
		}
	}

	@Override public void syncMovement(@NotNull Identifier stateId, int recoveryDelay, double efficiency) {
		super.syncMovement(stateId, recoveryDelay, efficiency);
		syncClientParagliding(state().paragliding());
	}

	@Override public void syncClientParagliding(boolean clientParagliding) {
		this.clientParagliding = clientParagliding;
	}

	@Override public void syncCanUseParaglider(boolean canUseParaglider, boolean canRideUpdraft) {
		this.canUseParaglider = canUseParaglider;
		this.canRideUpdraft = canRideUpdraft;
	}
}
