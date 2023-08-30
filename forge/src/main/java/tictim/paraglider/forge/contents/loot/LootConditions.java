package tictim.paraglider.forge.contents.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.forge.ForgeParagliderMod;
import tictim.paraglider.forge.contents.ForgeContents;

public enum LootConditions implements LootItemCondition, Serializer<LootItemCondition>{
	WITHER_DROPS_VESSEL,
	SPIRIT_ORB_LOOTS;

	@Override @NotNull public LootItemConditionType getType(){
		ForgeContents contents = ForgeParagliderMod.instance().getContents();
		return switch(this){
			case WITHER_DROPS_VESSEL -> contents.witherDropsVesselConfigCondition.get();
			case SPIRIT_ORB_LOOTS -> contents.spiritOrbLootsConfigCondition.get();
		};
	}
	@Override public boolean test(@NotNull LootContext lootContext){
		return switch(this){
			case WITHER_DROPS_VESSEL -> Cfg.get().witherDropsVessel();
			case SPIRIT_ORB_LOOTS -> Cfg.get().spiritOrbLoots();
		};
	}

	@Override public void serialize(@NotNull JsonObject json, @NotNull LootItemCondition condition, @NotNull JsonSerializationContext ctx){}
	@Override @NotNull public LootItemCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx){
		return this;
	}
}
