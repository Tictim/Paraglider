package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.impl.movement.PlayerStateMap;

public record SyncPlayerStateMapMsg(@NotNull PlayerStateMap stateMap) implements Msg{
	@NotNull public static SyncPlayerStateMapMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncPlayerStateMapMsg(PlayerStateMap.read(buffer));
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		stateMap.write(buffer);
	}
}
