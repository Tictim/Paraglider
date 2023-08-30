package tictim.paraglider.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.network.NetUtils;

import java.util.Map;

public record SyncCatalogMsg(int sessionId, @NotNull Map<ResourceLocation, BargainCatalog> catalog) implements Msg{
	@NotNull public static SyncCatalogMsg read(@NotNull FriendlyByteBuf buffer){
		return new SyncCatalogMsg(buffer.readVarInt(), NetUtils.readCatalogs(buffer));
	}

	@Override public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(sessionId);
		NetUtils.writeCatalogs(buffer, catalog);
	}
}
