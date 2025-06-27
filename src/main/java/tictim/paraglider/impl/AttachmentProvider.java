package tictim.paraglider.impl;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.RemotePlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

public final class AttachmentProvider {
	private AttachmentProvider() {}

	public static @NotNull PlayerMovement createPlayerMovement(@NotNull Player player) {
		if (player instanceof ServerPlayer sp) return new ServerPlayerMovement(sp);
		if (FMLEnvironment.dist.isClient()) return ClientImpl.createPlayerMovement(player);
		return new RemotePlayerMovement(player);
	}

	private static final class ClientImpl {
		static @NotNull PlayerMovement createPlayerMovement(@NotNull Player player) {
			return player instanceof LocalPlayer localPlayer ?
					new ClientPlayerMovement(localPlayer) :
					new RemotePlayerMovement(player);
		}
	}
}
