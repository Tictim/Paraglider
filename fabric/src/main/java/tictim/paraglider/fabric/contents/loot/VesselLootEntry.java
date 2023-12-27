package tictim.paraglider.fabric.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;

import java.util.List;
import java.util.function.Consumer;

public class VesselLootEntry extends LootPoolSingletonContainer{
	public static final Codec<VesselLootEntry> CODEC = RecordCodecBuilder.create(instance -> singletonFields(instance).apply(instance, VesselLootEntry::new));
	public VesselLootEntry(int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions){
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
		return simpleBuilder(VesselLootEntry::new);
	}

}
