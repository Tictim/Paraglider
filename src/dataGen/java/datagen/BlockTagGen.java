package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BlockTagGen extends BlockTagsProvider {
	public BlockTagGen(PackOutput output,
	                   CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, ParagliderAPI.MODID);
	}

	@SuppressWarnings("unchecked")
	@Override protected void addTags(HolderLookup.Provider provider) {
		Contents contents = Contents.get();
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
				contents.goddessStatue.getKey(),
				contents.kakarikoGoddessStatue.getKey(),
				contents.goronGoddessStatue.getKey(),
				contents.ritoGoddessStatue.getKey(),
				contents.hornedStatue.getKey());

		tag(ParagliderTags.Blocks.STATUES_GODDESS).add(
				contents.goddessStatue.getKey(),
				contents.kakarikoGoddessStatue.getKey(),
				contents.goronGoddessStatue.getKey(),
				contents.ritoGoddessStatue.getKey());
		tag(ParagliderTags.Blocks.STATUES)
				.add(contents.hornedStatue.getKey())
				.addTag(ParagliderTags.Blocks.STATUES_GODDESS);

		tag(ParagliderTags.Blocks.WIND_CAN_PASS_THROUGH).addAll(
				BlockItemIds.COPPER_GRATE.asList().stream()
						.map(id -> id.block())
						.toList()
		);
	}
}
