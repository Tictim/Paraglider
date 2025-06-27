package tictim.paraglider.contents;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.BargainType;

public final class BargainTypeRegistry {
	private BargainTypeRegistry() {}

	public static final ResourceKey<Registry<BargainType>> REGISTRY_KEY = ResourceKey.createRegistryKey(ParagliderAPI.id("bargain_types"));

	public static @Nullable BargainType getFromID(@NotNull ServerLevel level, @NotNull ResourceLocation id) {
		return getFromID(level.registryAccess(), id);
	}

	public static @Nullable BargainType getFromID(@NotNull MinecraftServer server, @NotNull ResourceLocation id) {
		return getFromID(server.registryAccess(), id);
	}

	public static @Nullable BargainType getFromID(@NotNull RegistryAccess registryAccess, @NotNull ResourceLocation id) {
		return registryAccess.lookupOrThrow(REGISTRY_KEY).getValue(id);
	}
}
