package tictim.paraglider.impl.stamina;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaFactory;

@NullMarked
public final class BotWStaminaFactory implements StaminaFactory {
	private @Nullable BotWStaminaClientFactory clientFactory;

	@Override public Stamina createServerInstance(ServerPlayer player) {
		return new BotWStamina(player);
	}

	@Override public Stamina createRemoteInstance(Player player) {
		return new BotWStamina(player);
	}

	@Override public StaminaFactory.ClientFactory clientFactory() {
		if (this.clientFactory == null) this.clientFactory = new BotWStaminaClientFactory();
		return this.clientFactory;
	}

	public static final class BotWStaminaClientFactory implements StaminaFactory.ClientFactory {
		@Override public Stamina createLocalClientInstance(LocalPlayer player) {
			return new BotWStamina(player);
		}
	}
}
