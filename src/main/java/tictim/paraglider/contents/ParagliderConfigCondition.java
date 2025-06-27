package tictim.paraglider.contents;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.FeatureCfg;

import java.util.Locale;

public enum ParagliderConfigCondition implements ICondition {
	HEART_CONTAINER_ENABLED,
	STAMINA_VESSEL_ENABLED;

	private final MapCodec<ParagliderConfigCondition> codec = MapCodec.unit(this);

	@Override public @NotNull MapCodec<? extends ICondition> codec() {
		return codec;
	}

	@Override public boolean test(@NotNull IContext context) {
		return switch (ParagliderConfigCondition.this) {
			case HEART_CONTAINER_ENABLED -> FeatureCfg.get().enableHeartContainers();
			case STAMINA_VESSEL_ENABLED -> FeatureCfg.get().enableStaminaVessels();
		};
	}

	public static void register(DeferredRegister<MapCodec<? extends ICondition>> register) {
		for (ParagliderConfigCondition c : values()) {
			MapCodec<ParagliderConfigCondition> type = c.codec;
			register.register(c.name().toLowerCase(Locale.ROOT), () -> type);
		}
	}
}
