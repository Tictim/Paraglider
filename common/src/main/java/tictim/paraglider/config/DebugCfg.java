package tictim.paraglider.config;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;

public interface DebugCfg{
	@NotNull static DebugCfg get(){
		return ParagliderMod.instance().getDebugConfig();
	}

	boolean debugPlayerMovement();

	boolean traceMovementPacket();
	boolean traceVesselPacket();
	boolean traceBargainPacket();
	boolean traceWindPacket();
}
