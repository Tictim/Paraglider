package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends ItemTagsProvider{
	public ItemTagGen(@NotNull PackOutput output,
	                  @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider,
	                  @NotNull CompletableFuture<TagLookup<Block>> blockTags,
	                  @Nullable ExistingFileHelper existingFileHelper){
		super(output, lookupProvider, blockTags, ParagliderAPI.MODID, existingFileHelper);
	}

	@Override protected void addTags(@NotNull HolderLookup.Provider provider){
		copy(ParagliderTags.Blocks.STATUES, ParagliderTags.STATUES);
		copy(ParagliderTags.Blocks.STATUES_GODDESS, ParagliderTags.STATUES_GODDESS);

		Contents contents = Contents.get();
		tag(ParagliderTags.PARAGLIDERS).add(contents.paraglider(), contents.dekuLeaf());
	}
}
