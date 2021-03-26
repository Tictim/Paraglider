package tictim.paraglider.datagen;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.ModTags;

import javax.annotation.Nullable;

public class BlockTagGen extends BlockTagsProvider{
	public BlockTagGen(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper){
		super(generatorIn, ParagliderMod.MODID, existingFileHelper);
	}

	@Override protected void registerTags(){
		getOrCreateBuilder(ModTags.Blocks.STATUES).add(
				Contents.GODDESS_STATUE.get(),
				Contents.HORNED_STATUE.get(),
				Contents.KAKIRIKO_GODDESS_STATUE.get());
	}
}
