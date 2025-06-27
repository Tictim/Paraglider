package tictim.paraglider.api.bargain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

/**
 * Base type for all bargain recipes.
 */
public interface Bargain extends Recipe<Bargain.NoInput> {
	/**
	 * Type of the bargain. Corresponds to bargain type registered with datapacks.
	 *
	 * @return Type of the bargain
	 */
	@NotNull ResourceLocation getBargainType();

	/**
	 * Checks if this bargain is available for {@code player} on {@code pos} (optional).
	 * Note that the block position specified might not be loaded or valid.
	 *
	 * @param player Player
	 * @param pos    Optional block position
	 * @return Whether this bargain is available
	 */
	boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos);

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
	@NotNull BargainResult bargain(@NotNull Player player, boolean simulate);

	/**
	 * @return List of preview for demands. This value is used both in server and client side, and the values
	 * on both sides must be same size, with identical arrangement.
	 */
	@NotNull @Unmodifiable List<@NotNull BargainPreview<?>> previewDemands();
	/**
	 * @return List of preview for offers. This value is used in client side.
	 */
	@NotNull @Unmodifiable List<@NotNull BargainPreview<?>> previewOffers();

	/**
	 * <p>
	 * Count how many instances of each input is supplied. For example, for item ingredient inputs the total number of
	 * items matched in inventory is returned; for heart containers the number of heart containers the player possesses
	 * is returned. The length of returned array should match the size of {@link #previewDemands()}, with each entry of
	 * same index corresponding to demand, to the count of how much input is currently supplied for that demand.
	 * </p>
	 * <p>
	 * Note that, because of the nature of checks, this method is called from server-side and sent to client.
	 * </p>
	 *
	 * @param player Player
	 * @return Array with values denoting how many instances of each input is supplied
	 */
	int @NotNull [] count(@NotNull Player player);

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
	@NotNull @Unmodifiable Set<@NotNull String> getBargainTags();

	// Methods from Recipe interface are completely useless for bargain recipes

	@Deprecated @Override default boolean matches(@NotNull NoInput input, @NotNull Level level) {
		return false;
	}

	@Deprecated
	@Override
	default @NotNull ItemStack assemble(@NotNull NoInput input, @NotNull HolderLookup.Provider lookup) {
		return ItemStack.EMPTY;
	}

	@Deprecated @Override default boolean isSpecial() {
		return true;
	}

	@Deprecated @Override default @NotNull PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	final class NoInput implements RecipeInput {
		@Override
		public @NotNull ItemStack getItem(int i) {
			return ItemStack.EMPTY;
		}

		@Override
		public int size() {
			return 0;
		}
	}
}
