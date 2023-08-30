package tictim.paraglider.fabric.contents.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;

import java.util.function.Consumer;

public class VesselLootEntry extends LootPoolSingletonContainer{
	public VesselLootEntry(int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions){
		super(weight, quality, conditions, functions);
	}

	@Override protected void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext){
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item!=null) consumer.accept(new ItemStack(item));
	}

	@Override @NotNull public LootPoolEntryType getType(){
		return ParagliderLoots.VESSEL_LOOT_ENTRY;
	}

	@NotNull public static LootPoolSingletonContainer.Builder<?> builder(){
		return simpleBuilder((weight, quality, conditions, functions) -> new VesselLootEntry(weight, quality, conditions, functions));
	}

	public static final class Serializer extends LootPoolSingletonContainer.Serializer<VesselLootEntry>{
		@Override @NotNull protected VesselLootEntry deserialize(JsonObject json,
		                                                         JsonDeserializationContext ctx,
		                                                         int weight,
		                                                         int quality,
		                                                         LootItemCondition[] conditions,
		                                                         LootItemFunction[] functions){
			return new VesselLootEntry(weight, quality, conditions, functions);
		}
	}
}
