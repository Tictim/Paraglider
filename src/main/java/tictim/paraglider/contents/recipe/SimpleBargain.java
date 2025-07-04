package tictim.paraglider.contents.recipe;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.*;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.recipe.preview.IngredientPreview;
import tictim.paraglider.contents.recipe.preview.ItemPreview;
import tictim.paraglider.contents.recipe.preview.VesselPreview;

import java.util.*;

public class SimpleBargain implements Bargain {
	private final ResourceLocation bargainType;

	private final List<QuantifiedIngredient> itemDemands;
	private final int heartContainerDemands;
	private final int staminaVesselDemands;
	private final int essenceDemands;

	private final List<QuantifiedItem> itemOffers;
	private final int heartContainerOffers;
	private final int staminaVesselOffers;
	private final int essenceOffers;

	private final Set<String> userTags;
	private final Set<String> tags;

	private @Nullable List<BargainPreview<?>> demandPreviews;
	private @Nullable List<BargainPreview<?>> offerPreviews;

	public SimpleBargain(@NotNull ResourceLocation bargainType,
	                     @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
	                     int heartContainerDemands,
	                     int staminaVesselDemands,
	                     int essenceDemands,
	                     @NotNull List<@NotNull QuantifiedItem> itemOffers,
	                     int heartContainerOffers,
	                     int staminaVesselOffers,
	                     int essenceOffers,
	                     @NotNull Set<@NotNull String> userTags) {
		this.bargainType = bargainType;
		this.itemDemands = ImmutableList.copyOf(itemDemands);
		for (var itemDemand : this.itemDemands) Objects.requireNonNull(itemDemand);
		this.heartContainerDemands = Math.max(0, heartContainerDemands);
		this.staminaVesselDemands = Math.max(0, staminaVesselDemands);
		this.essenceDemands = Math.max(0, essenceDemands);
		this.itemOffers = ImmutableList.copyOf(itemOffers);
		this.heartContainerOffers = Math.max(0, heartContainerOffers);
		this.staminaVesselOffers = Math.max(0, staminaVesselOffers);
		this.essenceOffers = Math.max(0, essenceOffers);

		this.userTags = Set.copyOf(userTags);
		this.tags = new ObjectOpenHashSet<>(userTags);

		if (!this.itemDemands.isEmpty()) this.tags.add(ParagliderBargainTags.CONSUMES_ITEM);
		if (this.heartContainerDemands > 0) this.tags.add(ParagliderBargainTags.CONSUMES_HEART_CONTAINER);
		if (this.staminaVesselDemands > 0) this.tags.add(ParagliderBargainTags.CONSUMES_STAMINA_VESSEL);
		if (this.essenceDemands > 0) this.tags.add(ParagliderBargainTags.CONSUMES_ESSENCE);
		if (!this.itemOffers.isEmpty()) this.tags.add(ParagliderBargainTags.GIVES_ITEM);
		if (this.heartContainerOffers > 0) this.tags.add(ParagliderBargainTags.GIVES_HEART_CONTAINER);
		if (this.staminaVesselOffers > 0) this.tags.add(ParagliderBargainTags.GIVES_STAMINA_VESSEL);
		if (this.essenceOffers > 0) this.tags.add(ParagliderBargainTags.GIVES_ESSENCE);
	}

	@Override public @NotNull ResourceLocation getBargainType() {
		return bargainType;
	}

	public @NotNull List<QuantifiedIngredient> getItemDemands() {
		return itemDemands;
	}
	public int getHeartContainerDemands() {
		return heartContainerDemands;
	}
	public int getStaminaVesselDemands() {
		return staminaVesselDemands;
	}
	public int getEssenceDemands() {
		return essenceDemands;
	}

	public @NotNull List<QuantifiedItem> getItemOffers() {
		return itemOffers;
	}
	public int getHeartContainerOffers() {
		return heartContainerOffers;
	}
	public int getStaminaVesselOffers() {
		return staminaVesselOffers;
	}
	public int getEssenceOffers() {
		return essenceOffers;
	}

	public @NotNull Set<String> getUserTags() {
		return userTags;
	}

	@Override public boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos) {
		return true;
	}

	@Override public @NotNull @Unmodifiable List<@NotNull BargainPreview<?>> previewDemands() {
		if (this.demandPreviews != null) return this.demandPreviews;
		this.demandPreviews = new ArrayList<>();

		for (QuantifiedIngredient i : this.itemDemands) this.demandPreviews.add(new IngredientPreview(i));

		if (this.heartContainerDemands > 0) {
			this.demandPreviews.add(new VesselPreview(VesselPreview.VesselType.HEART_CONTAINER, this.heartContainerDemands));
		}

		if (this.staminaVesselDemands > 0) {
			this.demandPreviews.add(new VesselPreview(VesselPreview.VesselType.STAMINA_VESSEL, this.staminaVesselDemands));
		}

		if (this.essenceDemands > 0) {
			this.demandPreviews.add(new VesselPreview(VesselPreview.VesselType.ESSENCE, this.essenceDemands));
		}

		return this.demandPreviews;
	}

	@Override public @NotNull @Unmodifiable List<@NotNull BargainPreview<?>> previewOffers() {
		if (this.offerPreviews != null) return this.offerPreviews;
		this.offerPreviews = new ArrayList<>();

		for (QuantifiedItem i : this.itemOffers) this.offerPreviews.add(new ItemPreview(i));

		if (this.heartContainerOffers > 0) {
			this.offerPreviews.add(new VesselPreview(VesselPreview.VesselType.HEART_CONTAINER, this.heartContainerOffers));
		}

		if (this.staminaVesselOffers > 0) {
			this.offerPreviews.add(new VesselPreview(VesselPreview.VesselType.STAMINA_VESSEL, this.staminaVesselOffers));
		}

		if (this.essenceOffers > 0) {
			this.offerPreviews.add(new VesselPreview(VesselPreview.VesselType.ESSENCE, this.essenceOffers));
		}

		return this.offerPreviews;
	}

	@Override public int @NotNull [] countDemands(@NotNull Player player) {
		IntList list = new IntArrayList();

		for (QuantifiedIngredient i : this.itemDemands) {
			list.add(ParagliderUtils.countIngredient(player, i.ingredient()));
		}

		if (this.heartContainerDemands > 0) {
			list.add(VesselContainer.get(player).heartContainer());
		}

		if (this.staminaVesselDemands > 0) {
			list.add(VesselContainer.get(player).staminaVessel());
		}

		if (this.essenceDemands > 0) {
			list.add(VesselContainer.get(player).essence());
		}

		return list.toIntArray();
	}

	@Override public @NotNull BargainResult bargain(@NotNull Player player, boolean simulate) {
		VesselContainer container = VesselContainer.get(player);

		Set<String> reasons = new HashSet<>();

		Inventory inventory = player.getInventory();
		var consumptions = new Int2IntOpenHashMap();
		for (QuantifiedIngredient i : this.itemDemands) {
			if (!ParagliderUtils.calculateConsumption(i, inventory, consumptions)) {
				reasons.add(ParagliderFailReasons.NOT_ENOUGH_ITEMS);
				break;
			}
		}

		if (container.takeHeartContainers(this.heartContainerDemands, true, false) < this.heartContainerDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_HEARTS);
		if (container.takeStaminaVessels(this.staminaVesselDemands, true, false) < this.staminaVesselDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_STAMINA);
		if (container.takeEssences(this.essenceDemands, true, false) < this.essenceDemands)
			reasons.add(ParagliderFailReasons.NOT_ENOUGH_ESSENCES);

		if (!reasons.isEmpty()) return BargainResult.fail(reasons);

		int heartDiff = this.heartContainerOffers - this.heartContainerDemands;
		int staminaDiff = this.staminaVesselOffers - this.staminaVesselDemands;
		int essenceDiff = this.essenceOffers - this.essenceDemands;

		if (heartDiff > 0 && container.giveHeartContainers(heartDiff, true, false) < heartDiff)
			reasons.add(ParagliderFailReasons.HEART_FULL);
		if (staminaDiff > 0 && container.giveStaminaVessels(staminaDiff, true, false) < staminaDiff)
			reasons.add(ParagliderFailReasons.STAMINA_FULL);
		if (essenceDiff > 0 && container.giveEssences(essenceDiff, true, false) < essenceDiff)
			reasons.add(ParagliderFailReasons.ESSENCE_FULL);

		if (!reasons.isEmpty()) return BargainResult.fail(reasons);

		if (!simulate) {
			for (var e : consumptions.int2IntEntrySet()) {
				int i = e.getIntKey();
				int c = e.getIntValue();
				if (c <= 0) continue;

				ItemStack stack = inventory.getItem(i);
				if (stack.getCount() > c) stack.shrink(c);
				else {
					if (stack.getCount() != c)
						ParagliderMod.LOGGER.error("Quantity of item {} (slot number {}) differs from simulation.", stack, i);
					inventory.setItem(i, ItemStack.EMPTY);
				}
			}

			for (QuantifiedItem item : this.itemOffers) {
				ParagliderUtils.giveItem(player, item.getItemWithQuantity());
			}

			if (heartDiff != 0) {
				if (heartDiff > 0 ?
						container.giveHeartContainers(heartDiff, false, true) != heartDiff :
						container.takeHeartContainers(-heartDiff, false, true) != -heartDiff)
					ParagliderMod.LOGGER.error("Heart Container transaction of bargain failed to resolve after successful simulation.");
			}
			if (staminaDiff != 0) {
				if (staminaDiff > 0 ?
						container.giveStaminaVessels(staminaDiff, false, true) != staminaDiff :
						container.takeStaminaVessels(-staminaDiff, false, true) != -staminaDiff)
					ParagliderMod.LOGGER.error("Stamina Vessel transaction of bargain failed to resolve after successful simulation.");
			}
			if (essenceDiff != 0) {
				if (essenceDiff > 0 ?
						container.giveEssences(essenceDiff, false, true) != essenceDiff :
						container.takeEssences(-essenceDiff, false, true) != -essenceDiff)
					ParagliderMod.LOGGER.error("Essence transaction of bargain failed to resolve after successful simulation.");
			}
		}
		return BargainResult.success();
	}

	@Override public @NotNull @Unmodifiable Set<@NotNull String> getBargainTags() {
		return Collections.unmodifiableSet(this.tags);
	}

	@Override public @NotNull RecipeSerializer<SimpleBargain> getSerializer() {
		return Contents.get().bargainRecipeSerializer();
	}
	@Override public @NotNull RecipeType<Bargain> getType() {
		return Contents.get().bargainRecipeType();
	}
}
