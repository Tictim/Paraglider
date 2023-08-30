package datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

public class BlockTagGen extends FabricTagProvider.BlockTagProvider{
	public BlockTagGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
		super(output, registriesFuture);
	}

	@Override protected void addTags(HolderLookup.Provider provider){
		Contents contents = Contents.get();
		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(reverseLookup(contents.goddessStatue()),
						reverseLookup(contents.kakarikoGoddessStatue()),
						reverseLookup(contents.goronGoddessStatue()),
						reverseLookup(contents.ritoGoddessStatue()),
						reverseLookup(contents.hornedStatue()));

		tag(ParagliderTags.Blocks.STATUES_GODDESS).add(
				reverseLookup(contents.goddessStatue()),
				reverseLookup(contents.kakarikoGoddessStatue()),
				reverseLookup(contents.goronGoddessStatue()),
				reverseLookup(contents.ritoGoddessStatue()));
		tag(ParagliderTags.Blocks.STATUES)
				.add(reverseLookup(contents.hornedStatue()))
				.addTag(ParagliderTags.Blocks.STATUES_GODDESS);
	}
}
