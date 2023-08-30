package tictim.paraglider.forge.contents;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.contents.BargainTypeRegistry;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ForgeBargainTypeRegistry implements BargainTypeRegistry{
	private static final ForgeBargainTypeRegistry instance = new ForgeBargainTypeRegistry();

	@NotNull public static BargainTypeRegistry get(){
		return instance;
	}

	private static final ResourceKey<Registry<BargainType>> REGISTRY_KEY = ResourceKey.createRegistryKey(ParagliderAPI.id("bargain_types"));

	@SubscribeEvent
	public static void onDataPackRegister(DataPackRegistryEvent.NewRegistry event){
		event.dataPackRegistry(REGISTRY_KEY, BargainType.CODEC);
	}

	@Override @Nullable public BargainType getFromID(@NotNull RegistryAccess registryAccess, @NotNull ResourceLocation id){
		return registryAccess.registryOrThrow(REGISTRY_KEY).get(id);
	}
}
