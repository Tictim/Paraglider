package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class ItemTagGen extends BlockTagCopyingItemTagProvider {
	public ItemTagGen(PackOutput output,
	                  CompletableFuture<HolderLookup.Provider> lookupProvider,
	                  CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
		super(output, lookupProvider, blockTags, ParagliderAPI.MODID);
	}

	@SuppressWarnings("unchecked")
	@Override protected void addTags(HolderLookup.Provider provider) {
		copy(ParagliderTags.Blocks.STATUES, ParagliderTags.STATUES);
		copy(ParagliderTags.Blocks.STATUES_GODDESS, ParagliderTags.STATUES_GODDESS);

		Contents contents = Contents.get();

		tag(ItemTags.CAULDRON_CAN_REMOVE_DYE).add(contents.paraglider.getKey(), contents.dekuLeaf.getKey());
		tag(ParagliderTags.PARAGLIDERS).add(contents.paraglider.getKey(), contents.dekuLeaf.getKey());
	}
}
