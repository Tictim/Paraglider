package datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends FabricTagProvider.ItemTagProvider{
	public ItemTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture){
		super(output, completableFuture, new BlockTagGen(output, completableFuture)); // ?
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		copy(ParagliderTags.Blocks.STATUES, ParagliderTags.STATUES);
		copy(ParagliderTags.Blocks.STATUES_GODDESS, ParagliderTags.STATUES_GODDESS);

		Contents contents = Contents.get();
		tag(ParagliderTags.PARAGLIDERS).add(reverseLookup(contents.paraglider()), reverseLookup(contents.dekuLeaf()));
	}
}
