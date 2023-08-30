package tictim.paraglider.api.bargain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

/**
 * Base type for all bargain recipes.
 */
public interface Bargain extends Recipe<Bargain.NoInventory>{
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
	 * Paragliders mod can be found in {@link ParagliderFailReasons}.
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
	@NotNull @Unmodifiable List<@NotNull DemandPreview> previewDemands();
	/**
	 * @return List of preview for offers. This value is used in client side.
	 */
	@NotNull @Unmodifiable List<@NotNull OfferPreview> previewOffers();

	/**
	 * @return Set of string tags associated with this bargain recipe. Tags describe basic description of what this
	 * bargain is about; for instance, a tag {@code "consumes_item"} indicates the bargain recipe requires some kind of
	 * item. This property is used in dialog system to determine which dialog is to be displayed after successful or
	 * failed bargain.
	 * <p/>
	 * Default set of tags used by Paragliders mod can be found on {@link ParagliderBargainTags}. All custom bargain
	 * recipes are encouraged to support these tags, in addition to user-added tags via JSON.
	 * @see ParagliderBargainTags
	 */
	@NotNull @Unmodifiable Set<@NotNull String> getBargainTags();

	// Methods from Recipe interface are completely useless for bargain recipes

	@Deprecated @Override default boolean matches(@NotNull NoInventory inv, @NotNull Level level){
		return false;
	}
	@Deprecated @Override @NotNull default ItemStack assemble(@NotNull NoInventory container, @NotNull RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override default boolean canCraftInDimensions(int width, int height){
		return false;
	}
	@Deprecated @Override @NotNull default ItemStack getResultItem(@NotNull RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override @NotNull default NonNullList<ItemStack> getRemainingItems(NoInventory inv){
		return NonNullList.create();
	}
	@Deprecated @Override @NotNull default NonNullList<Ingredient> getIngredients(){
		return NonNullList.create();
	}
	@Deprecated @Override default boolean isSpecial(){
		return true;
	}
	@Deprecated @Override @NotNull default String getGroup(){
		return "";
	}

	/**
	 * Nothing to see here.
	 */
	final class NoInventory implements Container{
		private NoInventory(){}

		private static final NoInventory instance = new NoInventory();

		@NotNull public static NoInventory get(){
			return instance;
		}

		@Override public int getContainerSize(){
			return 0;
		}
		@Override public boolean isEmpty(){
			return true;
		}
		@Override @NotNull public ItemStack getItem(int index){
			return ItemStack.EMPTY;
		}
		@Override @NotNull public ItemStack removeItem(int index, int count){
			return ItemStack.EMPTY;
		}
		@Override @NotNull public ItemStack removeItemNoUpdate(int index){
			return ItemStack.EMPTY;
		}
		@Override public void setItem(int index, @NotNull ItemStack stack){}
		@Override public void setChanged(){}
		@Override public boolean stillValid(@NotNull Player player){
			return false;
		}
		@Override public void clearContent(){}
	}
}
