package datagen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import tictim.paraglider.contents.Contents;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider{
	public LootTableGen(DataGenerator dataGeneratorIn){
		super(dataGeneratorIn);
	}

	@Override protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables(){
		return Collections.singletonList(Pair.of(BlockTables::new, LootParameterSets.BLOCK));
	}

	@Override protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker){}


	public static class BlockTables extends BlockLootTables{
		@Override protected void addTables(){
			registerDropSelfLootTable(Contents.GODDESS_STATUE.get());
			registerDropSelfLootTable(Contents.GORON_GODDESS_STATUE.get());
			registerDropSelfLootTable(Contents.KAKARIKO_GODDESS_STATUE.get());
			registerDropSelfLootTable(Contents.RITO_GODDESS_STATUE.get());
			registerDropSelfLootTable(Contents.HORNED_STATUE.get());
		}

		@Override protected Iterable<Block> getKnownBlocks(){
			return Contents.BLOCKS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList());
		}
	}
}
