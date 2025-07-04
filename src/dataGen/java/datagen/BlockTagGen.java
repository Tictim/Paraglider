package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import java.util.concurrent.CompletableFuture;

public class BlockTagGen extends BlockTagsProvider {
	public BlockTagGen(@NotNull PackOutput output,
	                   @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider,
	                   ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, ParagliderAPI.MODID, existingFileHelper);
	}

	@Override protected void addTags(@NotNull HolderLookup.Provider provider) {
		Contents contents = Contents.get();
		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(contents.goddessStatue(),
						contents.kakarikoGoddessStatue(),
						contents.goronGoddessStatue(),
						contents.ritoGoddessStatue(),
						contents.hornedStatue());

		tag(ParagliderTags.Blocks.STATUES_GODDESS).add(
				contents.goddessStatue(),
				contents.kakarikoGoddessStatue(),
				contents.goronGoddessStatue(),
				contents.ritoGoddessStatue());
		tag(ParagliderTags.Blocks.STATUES)
				.add(contents.hornedStatue())
				.addTag(ParagliderTags.Blocks.STATUES_GODDESS);

		tag(ParagliderTags.Blocks.WIND_CAN_PASS_THROUGH).add(
				Blocks.COPPER_GRATE,
				Blocks.EXPOSED_COPPER_GRATE,
				Blocks.WEATHERED_COPPER_GRATE,
				Blocks.OXIDIZED_COPPER_GRATE,
				Blocks.WAXED_COPPER_GRATE,
				Blocks.WAXED_EXPOSED_COPPER_GRATE,
				Blocks.WAXED_WEATHERED_COPPER_GRATE,
				Blocks.WAXED_OXIDIZED_COPPER_GRATE
		);
	}
}
