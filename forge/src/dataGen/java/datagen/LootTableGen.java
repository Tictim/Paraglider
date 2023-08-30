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
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.forge.ForgeParagliderMod;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LootTableGen extends LootTableProvider{
	public LootTableGen(@NotNull PackOutput output){
		super(output, Set.of(), List.of(new SubProviderEntry(
				() -> new BlockTables(),
				LootContextParamSets.BLOCK
		)));
	}

	@Override protected void validate(@NotNull Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationContext){}

	public static class BlockTables extends BlockLootSubProvider{
		public BlockTables(){
			super(Set.of(), FeatureFlags.REGISTRY.allFlags());
		}

		@Override protected void generate(){
			Contents contents = Contents.get();
			dropSelf(contents.goddessStatue());
			dropSelf(contents.goronGoddessStatue());
			dropSelf(contents.kakarikoGoddessStatue());
			dropSelf(contents.ritoGoddessStatue());
			dropSelf(contents.hornedStatue());
		}
		@Override @NotNull protected Iterable<Block> getKnownBlocks(){
			return ForgeParagliderMod.instance().getContents().blocks().getEntries().stream()
					.map(RegistryObject::get).toList();
		}
	}
}
