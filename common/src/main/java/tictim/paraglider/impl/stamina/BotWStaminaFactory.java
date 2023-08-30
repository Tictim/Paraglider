package tictim.paraglider.impl.stamina;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaFactory;
import tictim.paraglider.api.vessel.VesselContainer;

public final class BotWStaminaFactory implements StaminaFactory{
	@Override @NotNull public Stamina createServerInstance(@NotNull ServerPlayer player){
		return create(player);
	}
	@Override @NotNull public Stamina createRemoteInstance(@NotNull Player player){
		return create(player);
	}
	@Environment(EnvType.CLIENT)
	@Override @NotNull public Stamina createLocalClientInstance(@NotNull LocalPlayer player){
		return create(player);
	}

	@NotNull private Stamina create(@NotNull Player player){
		return new BotWStamina(VesselContainer.get(player));
	}
}
