package tictim.paraglider.network;

import tictim.paraglider.config.DebugCfg;

public enum Kind{
	MOVEMENT,
	VESSEL,
	BARGAIN,
	WIND;

	public boolean isTraceEnabled(){
		DebugCfg cfg = DebugCfg.get();
		return switch(this){
			case MOVEMENT -> cfg.traceMovementPacket();
			case VESSEL -> cfg.traceVesselPacket();
			case BARGAIN -> cfg.traceBargainPacket();
			case WIND -> cfg.traceWindPacket();
		};
	}
}
