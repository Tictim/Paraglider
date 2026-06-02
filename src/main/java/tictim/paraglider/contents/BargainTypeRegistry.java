package tictim.paraglider.contents;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.BargainType;

@NullMarked
public final class BargainTypeRegistry {
	private BargainTypeRegistry() {}

	public static final ResourceKey<Registry<BargainType>> REGISTRY_KEY = ResourceKey.createRegistryKey(ParagliderAPI.id("bargain_types"));

	public static @Nullable BargainType getFromID(RegistryAccess registryAccess, Identifier id) {
		return registryAccess.lookupOrThrow(REGISTRY_KEY).getValue(id);
	}
}
