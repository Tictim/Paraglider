package tictim.paraglider.contents;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import tictim.paraglider.ParagliderMod;

public final class ModTags{
	private ModTags(){}

	public static final Tags.IOptionalNamedTag<Item> PARAGLIDERS = ItemTags.createOptional(new ResourceLocation(ParagliderMod.MODID, "paragliders"));
	public static final Tags.IOptionalNamedTag<Item> STATUES = ItemTags.createOptional(new ResourceLocation(ParagliderMod.MODID, "statues"));

	public static final class Blocks {
		private Blocks(){}

		public static final Tags.IOptionalNamedTag<Block> STATUES = BlockTags.createOptional(new ResourceLocation(ParagliderMod.MODID, "statues"));
	}
}
