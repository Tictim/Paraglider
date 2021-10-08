package tictim.paraglider.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;

public class BlockTagGen extends BlockTagsProvider{
	public BlockTagGen(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper){
		super(generatorIn, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void addTags(){
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
