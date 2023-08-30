package tictim.paraglider.network;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface SyncMovementHandle{
	void syncMovement(@NotNull ResourceLocation stateId, int stamina, boolean depleted, int recoveryDelay);

	default void syncRemoteMovement(@NotNull ResourceLocation stateId){
		syncMovement(stateId, 0, false, 0);
	}
}
