package tictim.paraglider.fabric.contents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.contents.BargainTypeRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FabricBargainTypeRegistry implements BargainTypeRegistry, SimpleSynchronousResourceReloadListener{
	private FabricBargainTypeRegistry(){}

	private static final ResourceLocation ID = ParagliderAPI.id("bargain_types");
	private static final Gson GSON = new GsonBuilder().setLenient().create();

	private static final String BASE_PATH = ParagliderAPI.MODID+"/bargain_types";
	private static final Pattern PATH_REGEX = Pattern.compile("^"+BASE_PATH+"/(.+)\\.json$");

	private static final FabricBargainTypeRegistry instance = new FabricBargainTypeRegistry();

	@NotNull public static FabricBargainTypeRegistry get(){
		return instance;
	}

	private final Map<ResourceLocation, BargainType> bargainTypes = new Object2ObjectOpenHashMap<>();

	@Override @NotNull public ResourceLocation getFabricId(){
		return ID;
	}

	@Override public void onResourceManagerReload(@NotNull ResourceManager resources){
		this.bargainTypes.clear();

		for(var e : resources.listResources(
				ID.getNamespace()+"/"+ID.getPath(),
				path -> path.getPath().endsWith(".json")).entrySet()){
			Matcher m = PATH_REGEX.matcher(e.getKey().getPath());
			if(!m.matches()){
				ParagliderMod.LOGGER.error("Cannot read bargain type at {}: Invalid path", e.getKey());
				continue;
			}
			ResourceLocation id = new ResourceLocation(e.getKey().getNamespace(), m.group(1));
			try(InputStream stream = e.getValue().open()){
				JsonElement json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
				BargainType.CODEC.parse(JsonOps.INSTANCE, json).get().map(bargainType -> {
					ParagliderMod.LOGGER.debug("Read bargain type {}: {}", id, bargainType);
					this.bargainTypes.put(id, bargainType);
					return null;
				}, err -> {
					ParagliderMod.LOGGER.error("Cannot read bargain type {}: {}", id, err);
					return null;
				});
			}catch(IOException ex){
				ParagliderMod.LOGGER.error("Cannot read bargain type {}", id, ex);
			}
		}
	}

	@Override @Nullable public BargainType getFromID(@NotNull RegistryAccess registryAccess, @NotNull ResourceLocation id){
		return bargainTypes.get(id);
	}
}
