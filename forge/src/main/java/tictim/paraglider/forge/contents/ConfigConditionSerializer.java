package tictim.paraglider.forge.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.FeatureCfg;

public enum ConfigConditionSerializer implements ICondition {
	HEART_CONTAINER_ENABLED,
	STAMINA_VESSEL_ENABLED;

	private final Codec<ConfigConditionSerializer> codec = RecordCodecBuilder.create(instance -> instance.stable(this));

	@NotNull
	public Codec<ConfigConditionSerializer> codec() {
		return codec;
	}

	@Override public boolean test(IContext context){
		return switch(ConfigConditionSerializer.this){
			case HEART_CONTAINER_ENABLED -> FeatureCfg.get().enableHeartContainers();
			case STAMINA_VESSEL_ENABLED -> FeatureCfg.get().enableStaminaVessels();
		};
	}
}
