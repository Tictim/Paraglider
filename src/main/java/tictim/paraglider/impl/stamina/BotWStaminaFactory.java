package tictim.paraglider.impl.stamina;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaFactory;

public final class BotWStaminaFactory implements StaminaFactory {
	@Override public @NotNull Stamina createServerInstance(@NotNull ServerPlayer player) {
		return new BotWStamina(player);
	}

	@Override public @NotNull Stamina createRemoteInstance(@NotNull Player player) {
		return new BotWStamina(player);
	}

	@OnlyIn(Dist.CLIENT)
	@Override public @NotNull Stamina createLocalClientInstance(@NotNull LocalPlayer player) {
		return new BotWStamina(player);
	}
}
