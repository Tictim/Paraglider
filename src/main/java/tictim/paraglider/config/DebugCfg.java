package tictim.paraglider.config;

import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;

@NullMarked
public interface DebugCfg {
	static DebugCfg get() {
		return ParagliderMod.instance().getDebugConfig();
	}

	boolean debugPlayerMovement();

	boolean traceMovementPacket();
	boolean traceVesselPacket();
	boolean traceBargainPacket();
	boolean traceWindPacket();

	boolean verboseWindSourceLoading();
}
