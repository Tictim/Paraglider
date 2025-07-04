package tictim.paraglider.contents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface CommonContents {
	int PARAGLIDER_DEFAULT_COLOR = 0xFFA65955;
	int DEKU_LEAF_DEFAULT_COLOR = 0xFF3FB53F;

	static Item.Properties p() {
		return new Item.Properties();
	}

	static Item.Properties staminaPotion() {
		return p().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE);
	}

	static BlockBehaviour.Properties statueBlock() {
		return Block.Properties.of()
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
