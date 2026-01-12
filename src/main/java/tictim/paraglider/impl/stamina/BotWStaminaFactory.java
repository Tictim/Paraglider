package tictim.paraglider.impl.stamina;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaFactory;

public final class BotWStaminaFactory implements StaminaFactory {
	private BotWStaminaClientFactory clientFactory;

	@Override public @NotNull Stamina createServerInstance(@NotNull ServerPlayer player) {
		return new BotWStamina(player);
	}

	@Override public @NotNull Stamina createRemoteInstance(@NotNull Player player) {
		return new BotWStamina(player);
	}

	@Override public @NotNull StaminaFactory.ClientFactory clientFactory() {
		if (this.clientFactory == null) this.clientFactory = new BotWStaminaClientFactory();
		return this.clientFactory;
	}

	public static final class BotWStaminaClientFactory implements StaminaFactory.ClientFactory {
		@Override public @NotNull Stamina createLocalClientInstance(@NotNull LocalPlayer player) {
			return new BotWStamina(player);
		}
	}
}
