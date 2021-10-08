package tictim.paraglider.network;

import net.minecraft.network.FriendlyByteBuf;
import tictim.paraglider.wind.WindChunk;

public record SyncWindMsg(WindChunk windChunk){
	public static SyncWindMsg read(FriendlyByteBuf buf){
		return new SyncWindMsg(new WindChunk(buf));
	}

	public void write(FriendlyByteBuf buf){
		windChunk.write(buf);
	}
}
