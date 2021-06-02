package tictim.paraglider.recipe.bargain;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public interface StatueBargain extends IRecipe<NoInventory>{
	ResourceLocation getBargainOwner();

	BargainPreview getPreview();

	BargainResult bargain(PlayerEntity player, boolean simulate);

	boolean consumesItem();
	boolean consumesHeartContainer();
	boolean consumesStaminaVessel();
	boolean consumesEssence();
	boolean givesItem();
	boolean givesHeartContainer();
	boolean givesStaminaVessel();
	boolean givesEssence();

	// And behold, the peak of human intelligence

	@Deprecated @Override default boolean matches(NoInventory inv, World worldIn){
		return false;
	}
	@Deprecated @Override default ItemStack getCraftingResult(NoInventory inv){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override default boolean canFit(int width, int height){
		return false;
	}
	@Deprecated @Override default ItemStack getRecipeOutput(){
		return ItemStack.EMPTY;
	}
	@Deprecated @Override default NonNullList<ItemStack> getRemainingItems(NoInventory inv){
		return NonNullList.create();
	}
	@Deprecated @Override default NonNullList<Ingredient> getIngredients(){
		return NonNullList.create();
	}
	@Deprecated @Override default boolean isDynamic(){
		return true;
	}
	@Deprecated @Override default String getGroup(){
		return "";
	}
}
