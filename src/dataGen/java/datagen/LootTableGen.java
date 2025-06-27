package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.Contents;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootTableGen extends LootTableProvider {
	public LootTableGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, Set.of(), List.of(new SubProviderEntry(
				BlockTables::new,
				LootContextParamSets.BLOCK
		)), registries);
	}

	public static class BlockTables extends BlockLootSubProvider {
		public BlockTables(HolderLookup.Provider registries) {
			super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
		}

		@Override protected void generate() {
			Contents contents = Contents.get();
			dropSelf(contents.goddessStatue());
			dropSelf(contents.goronGoddessStatue());
			dropSelf(contents.kakarikoGoddessStatue());
			dropSelf(contents.ritoGoddessStatue());
			dropSelf(contents.hornedStatue());
		}

		@Override protected @NotNull Iterable<Block> getKnownBlocks() {
			return Contents.get().blocks.getEntries().stream()
					.map(h -> (Block)h.get())
					.toList();
		}
	}
}
