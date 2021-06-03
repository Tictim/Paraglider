package tictim.paraglider.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class CosmeticRecipe implements ICraftingRecipe{
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

	@Override public boolean matches(CraftingInventory inv, World worldIn){
		boolean paragliderSeen = false, reagentSeen = false;
		for(int i = 0; i<inv.getSizeInventory(); i++){
			ItemStack stack = inv.getStackInSlot(i);
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

	@Override public ItemStack getCraftingResult(CraftingInventory inv){
		ItemStack paraglider = new ItemStack(recipeOut);
		for(int i = 0; i<inv.getSizeInventory(); i++){
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.isEmpty()) continue;
			if(!reagent.test(stack)&&input.test(stack)){
				if(stack.hasTag()) paraglider.setTag(stack.getTag());
				return paraglider;
			}
		}
		return paraglider;
	}

	@Override public boolean canFit(int width, int height){
		return width*height>=2;
	}
	@Override public ItemStack getRecipeOutput(){
		return new ItemStack(recipeOut);
	}

	@Override public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv){
		NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for(int i = 0; i<list.size(); ++i){
			ItemStack item = inv.getStackInSlot(i);
			if(reagent.test(item)){
				ItemStack copy = item.copy();
				copy.setCount(1);
				list.set(i, copy);
			}else if(item.hasContainerItem()) list.set(i, item.getContainerItem());
		}

		return list;
	}

	@Override public NonNullList<Ingredient> getIngredients(){
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(Ingredient.fromStacks(Arrays.stream(input.getMatchingStacks()).filter(it -> it.getItem()!=recipeOut).toArray(ItemStack[]::new)));
		list.add(reagent);
		return list;
	}
	@Override public String getGroup(){
		return group;
	}
	@Override public ResourceLocation getId(){
		return id;
	}
	@Override public IRecipeSerializer<?> getSerializer(){
		return Contents.COSMETIC_RECIPE.get();
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CosmeticRecipe>{
		@Override public CosmeticRecipe read(ResourceLocation recipeId, JsonObject json){
			String group = JSONUtils.getString(json, "group", "");
			ResourceLocation itemName = new ResourceLocation(JSONUtils.getString(json, "result"));
			Item item = ForgeRegistries.ITEMS.getValue(itemName);
			if(item==null||!Objects.equals(item.getRegistryName(), itemName))
				throw new JsonSyntaxException("Unknown item '"+group+"'");
			Ingredient input = Ingredient.deserialize(json.get("input"));
			Ingredient reagent = Ingredient.deserialize(json.get("reagent"));
			return new CosmeticRecipe(recipeId, group, input, reagent, item);
		}

		@Nullable @Override public CosmeticRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
			String group = buffer.readString(32767);
			Ingredient input = Ingredient.read(buffer);
			Ingredient reagent = Ingredient.read(buffer);
			Item out = Item.getItemById(buffer.readVarInt());
			return new CosmeticRecipe(recipeId, group, input, reagent, out);
		}

		@Override public void write(PacketBuffer buffer, CosmeticRecipe recipe){
			buffer.writeString(recipe.group);
			recipe.input.write(buffer);
			recipe.reagent.write(buffer);
			buffer.writeVarInt(Item.getIdFromItem(recipe.recipeOut));
		}
	}
}
