package tictim.paraglider.config;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;

public interface DebugCfg {
	static @NotNull DebugCfg get() {
		return ParagliderMod.instance().getDebugConfig();
	}

	boolean debugPlayerMovement();

	boolean traceMovementPacket();
	boolean traceVesselPacket();
	boolean traceBargainPacket();
	boolean traceWindPacket();

	boolean verboseWindSourceLoading();
}
