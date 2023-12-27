package tictim.paraglider.fabric.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.List;
import java.util.function.Consumer;

import static tictim.paraglider.config.Cfg.TotwCompatConfigOption.*;

public class ParagliderLootEntry extends LootPoolSingletonContainer{
	public static final Codec<ParagliderLootEntry> CODEC = RecordCodecBuilder.create(instance ->
		instance
			.group(Codec.BOOL.fieldOf("deku_leaf").forGetter(paragliderLootEntry -> paragliderLootEntry.dekuLeaf))
			.and(singletonFields(instance))
			.apply(instance, ParagliderLootEntry::new));
	public final boolean dekuLeaf;

	public ParagliderLootEntry(boolean dekuLeaf, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions){
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

}
