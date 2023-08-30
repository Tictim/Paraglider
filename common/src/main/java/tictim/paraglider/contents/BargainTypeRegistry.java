package tictim.paraglider.contents;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.bargain.BargainType;

public interface BargainTypeRegistry{
	@NotNull static BargainTypeRegistry get(){
		return ParagliderMod.instance().getBargainTypeRegistry();
	}

	@Nullable default BargainType getFromID(@NotNull ServerLevel level, @NotNull ResourceLocation id){
		return getFromID(level.registryAccess(), id);
	}

	@Nullable default BargainType getFromID(@NotNull MinecraftServer server, @NotNull ResourceLocation id){
		return getFromID(server.registryAccess(), id);
	}

	@Nullable BargainType getFromID(@NotNull RegistryAccess registryAccess, @NotNull ResourceLocation id);
}
