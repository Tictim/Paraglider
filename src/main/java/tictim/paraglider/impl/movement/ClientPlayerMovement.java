package tictim.paraglider.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.stamina.Stamina;

import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

public class ClientPlayerMovement extends RemotePlayerMovement {
	private boolean wasParagliding;

	public ClientPlayerMovement(@NotNull LocalPlayer player) {
		super(player);
	}

	@Override public @NotNull LocalPlayer player() {
		return (LocalPlayer)super.player();
	}

	@Override protected @NotNull Stamina createCustomStamina() {
		return ParagliderAPI.staminaFactory().createLocalClientInstance(player());
	}

	@Override public void update() {
		updateStamina();

		boolean paragliding = state().has(FLAG_PARAGLIDING);
		if (!player().isCreative() && stamina().isDepleted()) {
			player().setSprinting(false);
			player().setSwimming(false);
		} else if (this.wasParagliding != paragliding) {
			player().setSprinting(paragliding);
		}

		applyMovement();

		this.wasParagliding = paragliding;
	}
}
