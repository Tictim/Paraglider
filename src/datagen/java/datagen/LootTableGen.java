package datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;
import tictim.paraglider.contents.Contents;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider{
	public LootTableGen(PackOutput output){
		super(output, Set.of(), List.of(new SubProviderEntry(
				() -> new BlockTables(),
				LootContextParamSets.BLOCK
		)));
	}

	@Override protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext){}

	public static class BlockTables extends BlockLootSubProvider{
		public BlockTables(){
			super(Set.of(), FeatureFlags.REGISTRY.allFlags());
		}

		@Override protected void generate(){
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
