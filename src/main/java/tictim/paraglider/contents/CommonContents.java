package tictim.paraglider.contents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface CommonContents {
	static Item.Properties p(ResourceLocation id) {
		return new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, id));
	}

	static Item.Properties staminaPotion(ResourceLocation id, ConsumeEffect... consumeEffects) {
		var consumable = Consumables.defaultDrink().consumeSeconds(0.6F);
		for (ConsumeEffect e : consumeEffects) consumable.onConsume(e);

		return p(id)
				.stacksTo(1)
				.component(DataComponents.CONSUMABLE, consumable.build())
				.craftRemainder(Items.GLASS_BOTTLE)
				.usingConvertsTo(Items.GLASS_BOTTLE);
	}

	static BlockBehaviour.Properties statueBlock(ResourceLocation id) {
		return Block.Properties.of()
				.setId(ResourceKey.create(Registries.BLOCK, id))
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.strength(1.5f, 100f)
				.noOcclusion();
	}

	static Component kakarikoStatueTooltip() {
		return Component.translatable("tooltip.paraglider.kakariko_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}

	static Component goronStatueTooltip() {
		return Component.translatable("tooltip.paraglider.goron_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}

	static Component ritoStatueTooltip() {
		return Component.translatable("tooltip.paraglider.rito_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}
}
