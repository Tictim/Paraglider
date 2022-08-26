package tictim.paraglider.contents.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import tictim.paraglider.ModCfg;

import java.util.Locale;
import java.util.function.BooleanSupplier;

import static tictim.paraglider.ParagliderMod.MODID;

public enum ConfigConditionSerializer implements IConditionSerializer<ICondition>{
	HEART_CONTAINER_ENABLED(ModCfg::enableHeartContainers),
	STAMINA_VESSEL_ENABLED(ModCfg::enableStaminaVessels);

	private final ResourceLocation id;
	private final BooleanSupplier config;

	ConfigConditionSerializer(BooleanSupplier config){
		this.id = new ResourceLocation(MODID, name().toLowerCase(Locale.ROOT));
		this.config = config;
	}

	@Override public void write(JsonObject json, ICondition value){}
	@Override public ICondition read(JsonObject json){
		return create();
	}
	@Override public ResourceLocation getID(){
		return id;
	}

	public ICondition create(){
		return new ICondition(){
			@Override public ResourceLocation getID(){
				return id;
			}
			@Override public boolean test(IContext context){
				return config.getAsBoolean();
			}
		};
	}
}
