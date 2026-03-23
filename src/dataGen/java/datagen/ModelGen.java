package datagen;

import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.client.ParaglidingItemProperty;
import tictim.paraglider.contents.Contents;

import java.util.function.BiConsumer;

import static net.minecraft.client.data.models.model.ItemModelUtils.conditional;
import static net.minecraft.client.data.models.model.ItemModelUtils.tintedModel;
import static tictim.paraglider.api.ParagliderAPI.MODID;

@NullMarked
public class ModelGen extends ModelProvider {
	public static final int PARAGLIDER_DEFAULT_COLOR = 0xFFA65955;
	public static final int DEKU_LEAF_DEFAULT_COLOR = 0xFF3FB53F;

	private final TexturedModel.Provider placeholderTextureModel = b -> new TexturedModel(TextureMapping.cube(b), ModelTemplates.CUBE_ALL) {
		@Override public Identifier create(Block block, BiConsumer<Identifier, ModelInstance> output) {
			return ModelLocationUtils.getModelLocation(block, "");
		}

		@Override public Identifier createWithSuffix(Block block, String suffix, BiConsumer<Identifier, ModelInstance> output) {
			return ModelLocationUtils.getModelLocation(block, suffix);
		}
	};

	public ModelGen(PackOutput output) {
		super(output, MODID);
	}

	@Override protected void registerModels(
			BlockModelGenerators blockModels,
			ItemModelGenerators itemModels
	) {
		Contents c = Contents.get();

		statueModel(blockModels, c.goddessStatue());
		statueModel(blockModels, c.kakarikoGoddessStatue());
		statueModel(blockModels, c.goronGoddessStatue());
		statueModel(blockModels, c.ritoGoddessStatue());
		statueModel(blockModels, c.hornedStatue());

		itemModels.itemModelOutput.accept(c.paraglider(), conditional(ParaglidingItemProperty.INSTANCE,
				tintedModel(ModelLocationUtils.getModelLocation(c.paraglider(), "_on"), new Dye(PARAGLIDER_DEFAULT_COLOR)),
				tintedModel(ModelLocationUtils.getModelLocation(c.paraglider()), new Dye(PARAGLIDER_DEFAULT_COLOR))
		));

		itemModels.itemModelOutput.accept(c.dekuLeaf(), conditional(ParaglidingItemProperty.INSTANCE,
				tintedModel(ModelLocationUtils.getModelLocation(c.dekuLeaf(), "_on"), new Dye(DEKU_LEAF_DEFAULT_COLOR)),
				tintedModel(ModelLocationUtils.getModelLocation(c.dekuLeaf()), new Dye(DEKU_LEAF_DEFAULT_COLOR))
		));

		itemModels.generateFlatItem(c.heartContainer(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.staminaVessel(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.spiritOrb(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.antiVessel(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.essence(), ModelTemplates.FLAT_ITEM);

		itemModels.generateFlatItem(c.energizingElixir1(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.energizingElixir2(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.energizingElixir3(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.enduringElixir1(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.enduringElixir2(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.enduringElixir3(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.energizingMixture(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(c.enduringMixture(), ModelTemplates.FLAT_ITEM);
	}

	private void statueModel(BlockModelGenerators blockModels, Block block) {
		blockModels.createHorizontallyRotatedBlock(block, this.placeholderTextureModel);
		blockModels.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
	}
}
