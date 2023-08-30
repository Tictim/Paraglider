package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.network.NetUtils;

import java.util.Map;

public record BargainInitMsg(
		int sessionId,
		@NotNull Map<ResourceLocation, BargainCatalog> catalog,
		@Nullable Vec3 lookAt,
		@Nullable Component dialog
) implements Msg{
	@NotNull public static BargainInitMsg read(@NotNull FriendlyByteBuf buffer){
		return new BargainInitMsg(
				buffer.readVarInt(),
				NetUtils.readCatalogs(buffer),
				NetUtils.readLookAt(buffer),
				buffer.readBoolean() ? buffer.readComponent() : null);
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
		NetUtils.writeCatalogs(buffer, catalog);
		NetUtils.writeLookAt(buffer, lookAt);
		buffer.writeBoolean(dialog!=null);
		if(dialog!=null) buffer.writeComponent(dialog);
	}
}
