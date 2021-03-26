package tictim.paraglider.datagen;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;

public class ItemTagGen extends ItemTagsProvider{
	public ItemTagGen(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, @Nullable ExistingFileHelper existingFileHelper){
		super(dataGenerator, blockTagProvider, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void registerTags(){
		copy(ModTags.Blocks.STATUES, ModTags.STATUES);

		getOrCreateBuilder(ModTags.PARAGLIDERS).add(Contents.PARAGLIDER.get(), Contents.DEKU_LEAF.get());
	}
}
