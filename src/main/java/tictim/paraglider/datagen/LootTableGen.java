package tictim.paraglider.datagen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;
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

	@Override protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables(){
		return Collections.singletonList(Pair.of(BlockTables::new, LootContextParamSets.BLOCK));
	}

	@Override protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext){}

	public static class BlockTables extends BlockLoot{
		@Override protected void addTables(){
			dropSelf(Contents.GODDESS_STATUE.get());
			dropSelf(Contents.GORON_GODDESS_STATUE.get());
			dropSelf(Contents.KAKARIKO_GODDESS_STATUE.get());
			dropSelf(Contents.RITO_GODDESS_STATUE.get());
			dropSelf(Contents.HORNED_STATUE.get());
		}

		@Override protected Iterable<Block> getKnownBlocks(){
			return Contents.BLOCKS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList());
		}
	}
}
