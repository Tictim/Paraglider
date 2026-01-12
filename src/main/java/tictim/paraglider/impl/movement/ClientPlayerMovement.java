package tictim.paraglider.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.stamina.Stamina;

public class ClientPlayerMovement extends RemotePlayerMovement {
	private boolean wasParagliding;

	public ClientPlayerMovement(@NotNull LocalPlayer player) {
		super(player);
	}

	@Override public @NotNull LocalPlayer player() {
		return (LocalPlayer) super.player();
	}

	@Override protected @NotNull Stamina createCustomStamina() {
		return ParagliderAPI.staminaFactory().clientFactory().createLocalClientInstance(player());
	}

	@Override public void update() {
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

		applyMovement();

		this.wasParagliding = paragliding;
	}
}
