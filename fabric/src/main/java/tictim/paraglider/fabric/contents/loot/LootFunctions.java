package tictim.paraglider.fabric.contents.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;

public enum LootFunctions implements LootItemFunction, Serializer<LootItemFunction>, LootItemFunction.Builder{
	SPAWNER_SPIRIT_ORB_COUNT;

	@Override @NotNull public LootItemFunctionType getType(){
		return switch(this){
			case SPAWNER_SPIRIT_ORB_COUNT -> ParagliderLoots.SPAWNER_SPIRIT_ORB_COUNT;
		};
	}
	@Override @NotNull public ItemStack apply(ItemStack stack, LootContext lootContext){
		return switch(this){
			case SPAWNER_SPIRIT_ORB_COUNT -> {
				stack.setCount(Cfg.get().spawnerSpiritOrbDrops());
				yield stack;
			}
		};
	}

	@Override public void serialize(JsonObject json, LootItemFunction function, JsonSerializationContext ctx){}
	@Override @NotNull public LootItemFunction deserialize(JsonObject json, JsonDeserializationContext ctx){
		return this;
	}

	@Override @NotNull public LootItemFunction build(){
		return this;
	}
}
