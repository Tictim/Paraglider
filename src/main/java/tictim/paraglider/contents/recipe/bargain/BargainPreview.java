package tictim.paraglider.contents.recipe.bargain;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tictim.paraglider.utils.TooltipFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record BargainPreview(List<Demand> demands, List<Offer> offers){
	@Override public String toString(){
		return "BargainPreview{"+
				"demands="+demands.stream().map(it -> it.toString()).collect(Collectors.joining(", "))+
				", offers="+offers.stream().map(it -> it.toString()).collect(Collectors.joining(", "))+
				'}';
	}

	public static final class Demand{
		private final ItemStack[] previewItems;
		private final int quantity;
		private final Counter counter;
		@Nullable private final TooltipFactory tooltipFactory;

		public Demand(ItemStack previewItem, int quantity, Counter counter){
			this(previewItem, quantity, counter, null);
		}
		public Demand(ItemStack[] previewItems, int quantity, Counter counter){
			this(previewItems, quantity, counter, null);
		}
		public Demand(ItemStack previewItem, int quantity, Counter counter, @Nullable TooltipFactory tooltipFactory){
			this(new ItemStack[]{previewItem}, quantity, counter, tooltipFactory);
		}
		public Demand(ItemStack[] previewItems, int quantity, Counter counter, @Nullable TooltipFactory tooltipFactory){
			this.previewItems = previewItems;
			this.quantity = quantity;
			this.counter = counter;
			this.tooltipFactory = tooltipFactory;
		}

		public ItemStack[] getPreviewItems(){
			return previewItems;
		}
		public int getQuantity(){
			return quantity;
		}
		public Counter getCounter(){
			return counter;
		}
		@Nullable public TooltipFactory getTooltipFactory(){
			return tooltipFactory;
		}

		@Override public String toString(){
			return "DemandPreview{"+
					"previewItems="+Arrays.toString(previewItems)+
					", quantity="+quantity+
					", counter="+counter+
					'}';
		}

		@FunctionalInterface
		public interface Counter{
			int count(Player player);
		}
	}

	public static final class Offer{
		private final ItemStack preview;
		private final int quantity;
		@Nullable private final TooltipFactory tooltipFactory;

		public Offer(ItemStack preview, int quantity){
			this(preview, quantity, null);
		}
		public Offer(ItemStack preview, int quantity, @Nullable TooltipFactory tooltipFactory){
			this.preview = preview;
			this.quantity = quantity;
			this.tooltipFactory = tooltipFactory;
		}

		public ItemStack getPreview(){
			return preview;
		}
		public int getQuantity(){
			return quantity;
		}
		@Nullable public TooltipFactory getTooltipFactory(){
			return tooltipFactory;
		}

		@Override public String toString(){
			return "OfferPreview{"+
					"preview="+preview+
					", quantity="+quantity+
					'}';
		}
	}
}
