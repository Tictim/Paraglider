package tictim.paraglider.fabric.contents.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.item.ParagliderItem;

import java.util.Arrays;
import java.util.function.Consumer;

import static tictim.paraglider.config.Cfg.TotwCompatConfigOption.*;

public class ParagliderLootEntry extends LootPoolSingletonContainer{
	public final boolean dekuLeaf;

	public ParagliderLootEntry(boolean dekuLeaf, int weight, int quality, LootItemCondition[] conditions, LootItemFunction[] functions){
		super(weight, quality, conditions, functions);
		this.dekuLeaf = dekuLeaf;
	}

	@Override protected void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext){
		Cfg.TotwCompatConfigOption configOption = Cfg.get().paragliderInTowersOfTheWild();
		if(configOption!=DISABLE){
			ParagliderItem item = configOption==DEKU_LEAF_ONLY||configOption!=PARAGLIDER_ONLY&&dekuLeaf ?
					Contents.get().dekuLeaf() : Contents.get().paraglider();
			ItemStack stack = new ItemStack(item);
			if(lootContext.getRandom().nextBoolean()){
				stack = DyeableLeatherItem.dyeArmor(stack, Arrays.asList(
						DyeItem.byColor(DyeColor.byId(lootContext.getRandom().nextInt(16))),
						DyeItem.byColor(DyeColor.byId(lootContext.getRandom().nextInt(16)))));
			}
			consumer.accept(stack);
		}
	}

	@Override @NotNull public LootPoolEntryType getType(){
		return ParagliderLoots.VESSEL_LOOT_ENTRY;
	}

	@NotNull public static LootPoolSingletonContainer.Builder<?> builder(boolean dekuLeaf){
		return simpleBuilder((weight, quality, conditions, functions) -> new ParagliderLootEntry(dekuLeaf, weight, quality, conditions, functions));
	}

	public static final class Serializer extends LootPoolSingletonContainer.Serializer<ParagliderLootEntry>{
		@Override public void serializeCustom(JsonObject json, ParagliderLootEntry entry, JsonSerializationContext ctx){
			super.serializeCustom(json, entry, ctx);
			json.addProperty("deku_leaf", entry.dekuLeaf);
		}

		@Override @NotNull protected ParagliderLootEntry deserialize(JsonObject json,
		                                                             JsonDeserializationContext ctx,
		                                                             int weight,
		                                                             int quality,
		                                                             LootItemCondition[] conditions,
		                                                             LootItemFunction[] functions){
			return new ParagliderLootEntry(GsonHelper.getAsBoolean(json, "deku_leaf"), weight, quality, conditions, functions);
		}
	}
}
