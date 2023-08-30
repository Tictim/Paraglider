package tictim.paraglider.api.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PlayerStateCondition{
	boolean test(@NotNull Player player, @NotNull PlayerState prevState, boolean canDoParagliding, double accumulatedFallDistance);
}
