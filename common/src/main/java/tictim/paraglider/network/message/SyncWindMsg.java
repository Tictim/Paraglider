package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.wind.WindChunk;

public record SyncWindMsg(@NotNull WindChunk windChunk) implements Msg{
	@NotNull public static SyncWindMsg read(@NotNull FriendlyByteBuf buf){
		return new SyncWindMsg(new WindChunk(buf));
	}

	@Override public void write(@NotNull FriendlyByteBuf buf){
		windChunk.write(buf);
	}
}
