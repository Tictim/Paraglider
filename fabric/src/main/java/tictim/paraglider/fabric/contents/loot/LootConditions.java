package tictim.paraglider.fabric.contents.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.FeatureCfg;

public enum LootConditions implements LootItemCondition, Serializer<LootItemCondition>, LootItemCondition.Builder{
	WITHER_DROPS_VESSEL,
	SPIRIT_ORB_LOOTS,
	FEATURES_SPIRIT_ORBS;

	@Override @NotNull public LootItemConditionType getType(){
		return switch(this){
			case WITHER_DROPS_VESSEL -> ParagliderLoots.WITHER_DROPS_VESSEL;
			case SPIRIT_ORB_LOOTS -> ParagliderLoots.SPIRIT_ORB_LOOTS;
			case FEATURES_SPIRIT_ORBS -> ParagliderLoots.FEATURES_SPIRIT_ORBS;
		};
	}
	@Override public boolean test(@NotNull LootContext lootContext){
		return switch(this){
			case WITHER_DROPS_VESSEL -> Cfg.get().witherDropsVessel();
			case SPIRIT_ORB_LOOTS -> Cfg.get().spiritOrbLoots();
			case FEATURES_SPIRIT_ORBS -> FeatureCfg.get().enableSpiritOrbGens();
		};
	}

	@Override public void serialize(@NotNull JsonObject json, @NotNull LootItemCondition condition, @NotNull JsonSerializationContext ctx){}
	@Override @NotNull public LootItemCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx){
		return this;
	}

	@Override @NotNull public LootItemCondition build(){
		return this;
	}
}
