package tictim.paraglider.network;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public interface SyncMovementHandle {
	void syncMovement(@NotNull Identifier stateId, int recoveryDelay, double efficiency);

	default void syncRemoteMovement(@NotNull Identifier stateId) {
		syncMovement(stateId, 0, 0);
	}
}
