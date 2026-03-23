package tictim.paraglider.network;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SyncMovementHandle {
	void syncMovement(Identifier stateId, int recoveryDelay, double efficiency);

	default void syncRemoteMovement(Identifier stateId) {
		syncMovement(stateId, 0, 0);
	}
}
