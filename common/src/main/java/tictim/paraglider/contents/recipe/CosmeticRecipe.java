package tictim.paraglider.contents.recipe;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.contents.Contents;

import java.util.Arrays;

public record CosmeticRecipe(@NotNull String group,
                             @NotNull Ingredient input,
                             @NotNull Ingredient reagent,
                             @NotNull Item recipeOut) implements CraftingRecipe{

	@Override public boolean matches(@NotNull CraftingContainer inv, @NotNull Level level){
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
	@Override @NotNull public ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess registryAccess){
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
	@Override @NotNull public ItemStack getResultItem(@NotNull RegistryAccess registryAccess){
		return new ItemStack(recipeOut);
	}

	@Override @NotNull public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inv){
		NonNullList<ItemStack> list = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for(int i = 0; i<list.size(); ++i){
			ItemStack stack = inv.getItem(i);
			if(reagent.test(stack)){
				ItemStack copy = stack.copy();
				copy.setCount(1);
				list.set(i, copy);
			}else{
				int i0 = i;
				ParagliderUtils.forRemainingItem(stack, rem -> list.set(i0, rem));
			}
		}

		return list;
	}

	@Override @NotNull public NonNullList<Ingredient> getIngredients(){
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(Ingredient.of(Arrays.stream(input.getItems()).filter(it -> it.getItem()!=recipeOut).toArray(ItemStack[]::new)));
		list.add(reagent);
		return list;
	}

	@Override @NotNull public RecipeSerializer<?> getSerializer(){
		return Contents.get().cosmeticRecipeSerializer();
	}
	@Override @NotNull public CraftingBookCategory category(){
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<CosmeticRecipe>{
		@Override @NotNull public Codec<CosmeticRecipe> codec(){
			return RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("group").orElse("").forGetter(CosmeticRecipe::getGroup),
					Ingredient.CODEC.fieldOf("input").forGetter(CosmeticRecipe::input),
					Ingredient.CODEC.fieldOf("reagent").forGetter(CosmeticRecipe::reagent),
					ResourceLocation.CODEC.fieldOf("result").xmap(id -> {
						Item item = ParagliderUtils.getItem(id);
						if(item==Items.AIR) throw new JsonSyntaxException("Unknown item '"+ id + "'");
						return item;
					}, ParagliderUtils::getKey).forGetter(CosmeticRecipe::recipeOut)
				).apply(instance, CosmeticRecipe::new)
			);
		}

		@Override @NotNull public CosmeticRecipe fromNetwork(@NotNull FriendlyByteBuf buffer){
			String group = buffer.readUtf();
			Ingredient input = Ingredient.fromNetwork(buffer);
			Ingredient reagent = Ingredient.fromNetwork(buffer);
			Item out = Item.byId(buffer.readVarInt());
			return new CosmeticRecipe(group, input, reagent, out);
		}

		@Override public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull CosmeticRecipe recipe){
			buffer.writeUtf(recipe.group);
			recipe.input.toNetwork(buffer);
			recipe.reagent.toNetwork(buffer);
			buffer.writeVarInt(Item.getId(recipe.recipeOut));
		}
	}
}
