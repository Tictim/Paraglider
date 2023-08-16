package tictim.paraglider.contents.recipe.bargain;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface StatueBargain extends Recipe<NoInventory>{
	ResourceLocation getBargainOwner();

	BargainPreview getPreview();

	BargainResult bargain(Player player, boolean simulate);

	boolean consumesItem();
	boolean consumesHeartContainer();
	boolean consumesStaminaVessel();
	boolean consumesEssence();
	boolean givesItem();
	boolean givesHeartContainer();
	boolean givesStaminaVessel();
	boolean givesEssence();

	// And behold, the peak of human intelligence

	@Deprecated @Override default boolean matches(NoInventory inv, Level worldIn){
		return false;
	}
	@Deprecated @Override default ItemStack assemble(NoInventory inv, RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override default boolean canCraftInDimensions(int width, int height){
		return false;
	}
	@Deprecated @Override default ItemStack getResultItem(RegistryAccess registryAccess){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override default NonNullList<ItemStack> getRemainingItems(NoInventory inv){
		return NonNullList.create();
	}
	@Deprecated @Override default NonNullList<Ingredient> getIngredients(){
		return NonNullList.create();
	}
	@Deprecated @Override default boolean isSpecial(){
		return true;
	}
	@Deprecated @Override default String getGroup(){
		return "";
	}
}
