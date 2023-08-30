package datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ParagliderTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BlockTagGen extends BlockTagsProvider{
	public BlockTagGen(@NotNull PackOutput output,
	                   @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider,
	                   @Nullable ExistingFileHelper existingFileHelper){
		super(output, lookupProvider, ParagliderAPI.MODID, existingFileHelper);
	}

	@Override protected void addTags(@NotNull HolderLookup.Provider provider){
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
	}
}
