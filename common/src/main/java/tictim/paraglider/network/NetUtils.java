package tictim.paraglider.network;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.bargain.BargainCatalog;

import java.util.Map;

public final class NetUtils{
	private NetUtils(){}

	@Nullable public static Vec3 readLookAt(@NotNull FriendlyByteBuf buffer){
		return buffer.readBoolean() ? new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()) : null;
	}

	public static void writeLookAt(@NotNull FriendlyByteBuf buffer, @Nullable Vec3 lookAt){
		buffer.writeBoolean(lookAt!=null);
		if(lookAt!=null){
			buffer.writeDouble(lookAt.x);
			buffer.writeDouble(lookAt.y);
			buffer.writeDouble(lookAt.z);
		}
	}

	@NotNull public static Map<ResourceLocation, BargainCatalog> readCatalogs(@NotNull FriendlyByteBuf buffer){
		Map<ResourceLocation, BargainCatalog> map = new Object2ObjectOpenHashMap<>();
		for(int i = 0, count = buffer.readVarInt(); i<count; i++){
			BargainCatalog c = BargainCatalog.read(buffer);
			map.put(c.bargain(), c);
		}
		return map;
	}

	public static void writeCatalogs(@NotNull FriendlyByteBuf buffer, @NotNull Map<ResourceLocation, BargainCatalog> previews){
		buffer.writeVarInt(previews.size());
		for(var e : previews.values()) e.write(buffer);
	}
}
