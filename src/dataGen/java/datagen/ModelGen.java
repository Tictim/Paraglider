package datagen;

import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.client.ParaglidingItemProperty;
import tictim.paraglider.contents.Contents;

import java.util.function.BiConsumer;

import static net.minecraft.client.data.models.model.ItemModelUtils.conditional;
import static net.minecraft.client.data.models.model.ItemModelUtils.tintedModel;
import static tictim.paraglider.api.ParagliderAPI.MODID;

public class ModelGen extends ModelProvider {
	public static final int PARAGLIDER_DEFAULT_COLOR = 0xFFA65955;
	public static final int DEKU_LEAF_DEFAULT_COLOR = 0xFF3FB53F;

	private final TexturedModel.Provider placeholderTextureModel = b -> new TexturedModel(TextureMapping.cube(b), ModelTemplates.CUBE_ALL) {
		@Override public @NotNull ResourceLocation create(
				@NotNull Block block,
				@NotNull BiConsumer<ResourceLocation, ModelInstance> output
		) {
			return ModelLocationUtils.getModelLocation(block, "");
		}

		@Override public @NotNull ResourceLocation createWithSuffix(
				@NotNull Block block, @NotNull String suffix,
				@NotNull BiConsumer<ResourceLocation, ModelInstance> output
		) {
			return ModelLocationUtils.getModelLocation(block, suffix);
		}
	};

	public ModelGen(PackOutput output) {
		super(output, MODID);
	}

	@Override protected void registerModels(
			@NotNull BlockModelGenerators blockModels,
			@NotNull ItemModelGenerators itemModels
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

		ResourceLocation staminaPotion = ParagliderAPI.id("item/stamina_potion");
		singleLayer(itemModels, c.staminaPotion1(), staminaPotion);
		singleLayer(itemModels, c.staminaPotion2(), staminaPotion);
		singleLayer(itemModels, c.staminaPotion3(), staminaPotion);

		ResourceLocation maxStaminaPotion = ParagliderAPI.id("item/max_stamina_potion");
		singleLayer(itemModels, c.maxStaminaPotion1(), maxStaminaPotion);
		singleLayer(itemModels, c.maxStaminaPotion2(), maxStaminaPotion);
		singleLayer(itemModels, c.maxStaminaPotion3(), maxStaminaPotion);
	}

	private void statueModel(BlockModelGenerators blockModels, Block block) {
		blockModels.createHorizontallyRotatedBlock(block, this.placeholderTextureModel);
		blockModels.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block));
	}

	private void singleLayer(ItemModelGenerators itemModels, Item item, ResourceLocation textureLocation) {
		itemModels.itemModelOutput.accept(item,
				ItemModelUtils.plainModel(
						ModelTemplates.FLAT_ITEM.create(item,
								TextureMapping.layer0(textureLocation),
								itemModels.modelOutput)
				)
		);
	}
}
