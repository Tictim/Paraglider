package tictim.paraglider.network;

import net.minecraft.network.PacketBuffer;
import tictim.paraglider.capabilities.wind.WindChunk;

public class SyncWindMsg{
	public static SyncWindMsg read(PacketBuffer buf){
		return new SyncWindMsg(new WindChunk(buf));
	}

	public final WindChunk windChunk;

	public SyncWindMsg(WindChunk windChunk){
		this.windChunk = windChunk;
	}

	public void write(PacketBuffer buf){
		windChunk.write(buf);
	}
}
