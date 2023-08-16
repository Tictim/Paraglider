package tictim.paraglider.contents.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.Arrays;

public class CosmeticRecipe implements CraftingRecipe{
	private final ResourceLocation id;
	private final String group;
	private final Ingredient input;
	private final Ingredient reagent;
	private final Item recipeOut;

	public CosmeticRecipe(ResourceLocation id, String group, Ingredient input, Ingredient reagent, Item recipeOut){
		this.id = id;
		this.group = group;
		this.input = input;
		this.reagent = reagent;
		this.recipeOut = recipeOut;
	}

	@Override public boolean matches(CraftingContainer inv, Level worldIn){
		boolean paragliderSeen = false, reagentSeen = false;
		for(int i = 0; i<inv.getContainerSize(); i++){
			ItemStack stack = inv.getItem(i);
			if(stack.isEmpty()) continue;
			if(reagent.test(stack)){
				if(reagentSeen) return false;
				else reagentSeen = true;
			}else if(input.test(stack)&&stack.getItem()!=recipeOut){
				if(paragliderSeen) return false;
				else paragliderSeen = true;
			}else return false;
		}
		return paragliderSeen&&reagentSeen;
	}

	@Override public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess){
		ItemStack paraglider = new ItemStack(recipeOut);
		for(int i = 0; i<inv.getContainerSize(); i++){
			ItemStack stack = inv.getItem(i);
			if(stack.isEmpty()) continue;
			if(!reagent.test(stack)&&input.test(stack)){
				if(stack.hasTag()) paraglider.setTag(stack.getTag());
				return paraglider;
			}
		}
		return paraglider;
	}

	@Override public boolean canCraftInDimensions(int width, int height){
		return width*height>=2;
	}
	@Override public ItemStack getResultItem(RegistryAccess registryAccess){
		return new ItemStack(recipeOut);
	}

	@Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv){
		NonNullList<ItemStack> list = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for(int i = 0; i<list.size(); ++i){
			ItemStack item = inv.getItem(i);
			if(reagent.test(item)){
				ItemStack copy = item.copy();
				copy.setCount(1);
				list.set(i, copy);
			}else if(item.hasCraftingRemainingItem()) list.set(i, item.getCraftingRemainingItem());
		}

		return list;
	}

	@Override public NonNullList<Ingredient> getIngredients(){
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(Ingredient.of(Arrays.stream(input.getItems()).filter(it -> it.getItem()!=recipeOut).toArray(ItemStack[]::new)));
		list.add(reagent);
		return list;
	}
	@Override public String getGroup(){
		return group;
	}
	@Override public ResourceLocation getId(){
		return id;
	}
	@Override public RecipeSerializer<?> getSerializer(){
		return Contents.COSMETIC_RECIPE.get();
	}
	@Override public CraftingBookCategory category(){
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<CosmeticRecipe>{
		@Override public CosmeticRecipe fromJson(ResourceLocation recipeId, JsonObject json){
			String group = GsonHelper.getAsString(json, "group", "");
			ResourceLocation itemName = new ResourceLocation(GsonHelper.getAsString(json, "result"));
			Item item = ForgeRegistries.ITEMS.getValue(itemName);
			if(item==null||item==Items.AIR)
				throw new JsonSyntaxException("Unknown item '"+group+"'");
			Ingredient input = Ingredient.fromJson(json.get("input"));
			Ingredient reagent = Ingredient.fromJson(json.get("reagent"));
			return new CosmeticRecipe(recipeId, group, input, reagent, item);
		}

		@Nullable @Override public CosmeticRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer){
			String group = buffer.readUtf(32767);
			Ingredient input = Ingredient.fromNetwork(buffer);
			Ingredient reagent = Ingredient.fromNetwork(buffer);
			Item out = Item.byId(buffer.readVarInt());
			return new CosmeticRecipe(recipeId, group, input, reagent, out);
		}

		@Override public void toNetwork(FriendlyByteBuf buffer, CosmeticRecipe recipe){
			buffer.writeUtf(recipe.group);
			recipe.input.toNetwork(buffer);
			recipe.reagent.toNetwork(buffer);
			buffer.writeVarInt(Item.getId(recipe.recipeOut));
		}
	}
}
