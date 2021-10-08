package tictim.paraglider.contents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModTags{
	private ModTags(){}

	public static final Tags.IOptionalNamedTag<Item> PARAGLIDERS = ItemTags.createOptional(new ResourceLocation(MODID, "paragliders"));
	public static final Tags.IOptionalNamedTag<Item> STATUES = ItemTags.createOptional(new ResourceLocation(MODID, "statues"));
	public static final Tags.IOptionalNamedTag<Item> STATUES_GODDESS = ItemTags.createOptional(new ResourceLocation(MODID, "statues/goddess"));

	public static final class Blocks{
		private Blocks(){}

		public static final Tags.IOptionalNamedTag<Block> STATUES = BlockTags.createOptional(new ResourceLocation(MODID, "statues"));
		public static final Tags.IOptionalNamedTag<Block> STATUES_GODDESS = BlockTags.createOptional(new ResourceLocation(MODID, "statues/goddess"));
	}
}
