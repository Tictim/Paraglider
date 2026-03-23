package tictim.paraglider.api.stamina;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NullMarked;

/**
 * Factory for {@link Stamina}.
 */
@NullMarked
public interface StaminaFactory {
	/**
	 * Create a new instance of {@link Stamina} for use in server environment.
	 *
	 * @param player Server player
	 * @return New {@link Stamina} instance
	 */
	Stamina createServerInstance(ServerPlayer player);

	/**
	 * Create a new instance of {@link Stamina} for use in remote environment. Additionally, non-{@link ServerPlayer}
	 * player instances in server environment also will receive this instance.
	 *
	 * @param player Player
	 * @return New {@link Stamina} instance
	 */
	Stamina createRemoteInstance(Player player);

	ClientFactory clientFactory();

	interface ClientFactory {
		/**
		 * Create a new instance of {@link Stamina} for use in remote environment. This instance is only attached to
		 * {@link LocalPlayer} instances.
		 *
		 * @param player Local player
		 * @return New {@link Stamina} instance
		 */
		Stamina createLocalClientInstance(LocalPlayer player);
	}
}
