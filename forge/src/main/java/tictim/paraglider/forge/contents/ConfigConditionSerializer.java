package tictim.paraglider.forge.contents;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.config.FeatureCfg;

import java.util.Locale;

public enum ConfigConditionSerializer implements IConditionSerializer<ICondition>, ICondition{
	HEART_CONTAINER_ENABLED,
	STAMINA_VESSEL_ENABLED;

	private final ResourceLocation id = ParagliderAPI.id(name().toLowerCase(Locale.ROOT));

	@Override public void write(@NotNull JsonObject json, @NotNull ICondition value){}
	@Override @NotNull public ICondition read(JsonObject json){
		return this;
	}
	@Override @NotNull public ResourceLocation getID(){
		return id;
	}

	@Override public boolean test(IContext context){
		return switch(ConfigConditionSerializer.this){
			case HEART_CONTAINER_ENABLED -> FeatureCfg.get().enableHeartContainers();
			case STAMINA_VESSEL_ENABLED -> FeatureCfg.get().enableStaminaVessels();
		};
	}
}
