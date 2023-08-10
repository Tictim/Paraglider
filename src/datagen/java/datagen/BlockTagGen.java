package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BlockTagGen extends BlockTagsProvider{
	public BlockTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper){
		super(output, lookupProvider, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(Contents.GODDESS_STATUE.get(),
						Contents.KAKARIKO_GODDESS_STATUE.get(),
						Contents.GORON_GODDESS_STATUE.get(),
						Contents.RITO_GODDESS_STATUE.get(),
						Contents.HORNED_STATUE.get());

		tag(ModTags.Blocks.STATUES_GODDESS).add(
				Contents.GODDESS_STATUE.get(),
				Contents.KAKARIKO_GODDESS_STATUE.get(),
				Contents.GORON_GODDESS_STATUE.get(),
				Contents.RITO_GODDESS_STATUE.get());
		tag(ModTags.Blocks.STATUES)
				.add(Contents.HORNED_STATUE.get())
				.addTag(ModTags.Blocks.STATUES_GODDESS);
	}
}
