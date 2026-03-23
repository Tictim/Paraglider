package tictim.paraglider.api.bargain;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Set;

/**
 * Base type for all bargain recipes.
 */
@NullMarked
public interface Bargain extends Recipe<Bargain.NoInput> {
	/**
	 * Type of the bargain. Corresponds to bargain type registered with datapacks.
	 *
	 * @return Type of the bargain
	 */
	Identifier getBargainType();

	/**
	 * Checks if this bargain is available for {@code player} on {@code pos} (optional).
	 * Note that the block position specified might not be loaded or valid.
	 *
	 * @param player Player
	 * @param pos    Optional block position
	 * @return Whether this bargain is available
	 */
	boolean isAvailableFor(Player player, @Nullable BlockPos pos);

	/**
	 * Tries to perform bargain with {@code player}. This method handles all necessary transactions for the bargain -
	 * taking away input items, giving output items and such. If the player fails to meet the bargain's conditions, the
	 * bargain is cancelled, and result object indicating failure is returned.
	 * <p/>
	 * A failed bargain can specify the reasons behind failure in result instance. This value is used in dialog system
	 * to determine which dialog is to be displayed after failed bargain. Because of this, all custom bargain recipes
	 * are encouraged to support and document list of possible failure reasons. Default set of failure reasons used by
	 * Paraglider mod can be found in {@link ParagliderFailReasons}.
	 *
	 * @param player   Player
	 * @param simulate If {@code true}, this operation will not affect the state of the game.
	 * @return Result instance
	 * @see ParagliderFailReasons
	 */
	BargainResult bargain(Player player, boolean simulate);

	/**
	 * @return List of preview for demands. The previews are recreated on server side and synced on initialization of
	 * bargain, as well as when the player's inventory or vessel amount changes.
	 * @see #countDemands(Player)
	 */
	@Unmodifiable List<BargainPreview<?>> previewDemands();

	/**
	 * @return List of preview for offers. The previews are recreated on server side and synced on initialization of
	 * bargain, as well as when the player's inventory or vessel amount changes.
	 */
	@Unmodifiable List<BargainPreview<?>> previewOffers();

	/**
	 * <p>
	 * Count how many instances of each input is supplied. For example, for item inputs the total number of items
	 * matched in inventory is returned; for heart containers the number of heart containers the player possesses is
	 * returned. The length of returned array should match the size of {@link #previewDemands()}, with each entry of
	 * same index corresponding to the demand, to the count of how much input is currently supplied for that demand.
	 * </p>
	 * <p>
	 * This value is evaluated on server-side and sent to client.
	 * </p>
	 *
	 * @param player Player
	 * @return Array with values denoting how many instances of each input is supplied
	 */
	int[] countDemands(Player player);

	/**
	 * @return Set of string tags associated with this bargain recipe. Tags describe basic description of what this
	 * bargain is about; for instance, a tag {@code "consumes_item"} indicates the bargain recipe requires some kind of
	 * item. This property is used in dialog system to determine which dialog is to be displayed after successful or
	 * failed bargain.
	 * <p/>
	 * Default set of tags used by Paraglider mod can be found on {@link ParagliderBargainTags}. All custom bargain
	 * recipes are encouraged to support these tags, in addition to user-added tags via JSON.
	 * @see ParagliderBargainTags
	 */
	@Unmodifiable Set<String> getBargainTags();

	// Methods from Recipe interface are completely useless for bargain recipes

	@Deprecated @Override default boolean matches(NoInput input, Level level) {
		return false;
	}

	@Deprecated @Override default ItemStack assemble(NoInput input) {
		return ItemStack.EMPTY;
	}

	@Deprecated @Override default boolean isSpecial() {
		return true;
	}

	@Override default boolean showNotification() {
		return false;
	}

	@Override default String group() {
		return "";
	}

	@Override default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	@Deprecated @Override default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	final class NoInput implements RecipeInput {
		@Override public ItemStack getItem(int i) {
			return ItemStack.EMPTY;
		}

		@Override public int size() {
			return 0;
		}
	}
}
