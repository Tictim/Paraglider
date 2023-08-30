package tictim.paraglider.api.stamina;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link Stamina}.
 */
public interface StaminaFactory{
	/**
	 * Create a new instance of {@link Stamina} for use in server environment.
	 *
	 * @param player Server player
	 * @return New {@link Stamina} instance
	 */
	@NotNull Stamina createServerInstance(@NotNull ServerPlayer player);

	/**
	 * Create a new instance of {@link Stamina} for use in remote environment. Additionally, non-{@link ServerPlayer}
	 * player instances in server environment also will receive this instance.
	 *
	 * @param player Player
	 * @return New {@link Stamina} instance
	 */
	@NotNull Stamina createRemoteInstance(@NotNull Player player);

	/**
	 * Create a new instance of {@link Stamina} for use in remote environment. This instance is only attached to
	 * {@link LocalPlayer} instances.
	 *
	 * @param player Local player
	 * @return New {@link Stamina} instance
	 */
	@Environment(EnvType.CLIENT)
	@NotNull Stamina createLocalClientInstance(@NotNull LocalPlayer player);
}
