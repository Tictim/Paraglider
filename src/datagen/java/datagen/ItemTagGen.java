package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends ItemTagsProvider{
	public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper){
		super(output, lookupProvider, blockTags, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		copy(ModTags.Blocks.STATUES, ModTags.STATUES);
		copy(ModTags.Blocks.STATUES_GODDESS, ModTags.STATUES_GODDESS);

		tag(ModTags.PARAGLIDERS).add(Contents.PARAGLIDER.get(), Contents.DEKU_LEAF.get());
	}
}
