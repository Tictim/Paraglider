package tictim.paraglider.contents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public interface CommonContents{
	int PARAGLIDER_DEFAULT_COLOR = 0xA65955;
	int DEKU_LEAF_DEFAULT_COLOR = 0x3FB53F;

	@NotNull static BlockBehaviour.Properties statueBlock(){
		return Block.Properties.of()
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.strength(1.5f, 100f)
				.noOcclusion();
	}

	@NotNull static Item.Properties uncommonItem(){
		return new Item.Properties().rarity(Rarity.UNCOMMON);
	}

	@NotNull static Item.Properties rareItem(){
		return new Item.Properties().rarity(Rarity.RARE);
	}

	@NotNull static Item.Properties epicItem(){
		return new Item.Properties().rarity(Rarity.EPIC);
	}

	@NotNull static Component kakarikoStatueTooltip(){
		return Component.translatable("tooltip.paraglider.kakariko_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}
	@NotNull static Component goronStatueTooltip(){
		return Component.translatable("tooltip.paraglider.goron_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}
	@NotNull static Component ritoStatueTooltip(){
		return Component.translatable("tooltip.paraglider.rito_goddess_statue.0")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
	}
}
