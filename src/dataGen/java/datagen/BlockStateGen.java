package datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.Contents;

public class BlockStateGen extends BlockStateProvider {
	public BlockStateGen(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, ParagliderAPI.MODID, existingFileHelper);
	}

	@Override protected void registerStatesAndModels() {
		Contents c = Contents.get();

		statueModel(c.goddessStatue());
		statueModel(c.kakarikoGoddessStatue());
		statueModel(c.goronGoddessStatue());
		statueModel(c.ritoGoddessStatue());
		statueModel(c.hornedStatue());
	}

	private void statueModel(Block block) {
		var model = new ModelFile.ExistingModelFile(
				ModelLocationUtils.getModelLocation(block),
				models().existingFileHelper);
		horizontalBlock(block, model);
		simpleBlockItem(block, model);
	}
}
