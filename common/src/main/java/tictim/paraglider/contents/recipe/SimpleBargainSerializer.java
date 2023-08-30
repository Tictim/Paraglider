package tictim.paraglider.contents.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class SimpleBargainSerializer<T extends SimpleBargain> implements RecipeSerializer<T>{
	@Override @NotNull public T fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json){
		final ResourceLocation bargainType = new ResourceLocation(
				json.has("bargainType") ? GsonHelper.getAsString(json, "bargainType") :
						json.has("owner") ? GsonHelper.getAsString(json, "owner") :
								GsonHelper.getAsString(json, "bargainType")); // print error for bargainType field
		final List<QuantifiedIngredient> itemDemands;
		final int heartContainerDemands;
		final int staminaVesselDemands;
		final int essenceDemands;
		final List<QuantifiedItem> itemOffers;
		final int heartContainerOffers;
		final int staminaVesselOffers;
		final int essenceOffers;
		final Set<String> userTags;

		JsonObject demands = GsonHelper.getAsJsonObject(json, "demands", null);
		if(demands!=null){
			JsonArray items = GsonHelper.getAsJsonArray(demands, "items", null);
			if(items==null||items.isEmpty()) itemDemands = Collections.emptyList();
			else{
				itemDemands = new ArrayList<>();
				for(JsonElement i : items)
					itemDemands.add(new QuantifiedIngredient(GsonHelper.convertToJsonObject(i, "item")));
			}
			heartContainerDemands = Math.max(0, GsonHelper.getAsInt(demands, "heartContainers", 0));
			staminaVesselDemands = Math.max(0, GsonHelper.getAsInt(demands, "staminaVessels", 0));
			essenceDemands = Math.max(0, GsonHelper.getAsInt(demands, "essences", 0));
		}else{
			itemDemands = Collections.emptyList();
			heartContainerDemands = 0;
			staminaVesselDemands = 0;
			essenceDemands = 0;
		}

		JsonObject offers = GsonHelper.getAsJsonObject(json, "offers", null);
		if(offers!=null){
			JsonArray items = GsonHelper.getAsJsonArray(offers, "items", null);
			if(items==null||items.isEmpty()) itemOffers = Collections.emptyList();
			else{
				itemOffers = new ArrayList<>();
				for(JsonElement i : items){
					ItemStack stack = ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(i, "item"));
					itemOffers.add(new QuantifiedItem(stack.getItem(), stack.getCount()));
				}
			}
			heartContainerOffers = Math.max(0, GsonHelper.getAsInt(offers, "heartContainers", 0));
			staminaVesselOffers = Math.max(0, GsonHelper.getAsInt(offers, "staminaVessels", 0));
			essenceOffers = Math.max(0, GsonHelper.getAsInt(offers, "essences", 0));
		}else{
			itemOffers = Collections.emptyList();
			heartContainerOffers = 0;
			staminaVesselOffers = 0;
			essenceOffers = 0;
		}

		JsonArray tags = GsonHelper.getAsJsonArray(json, "tags", null);
		if(tags!=null){
			userTags = new ObjectOpenHashSet<>();
			for(JsonElement tag : tags){
				userTags.add(GsonHelper.convertToString(tag, "tag"));
			}
		}else{
			userTags = Collections.emptySet();
		}

		return instantiate(recipeId,
				json,
				bargainType,
				itemDemands,
				heartContainerDemands,
				staminaVesselDemands,
				essenceDemands,
				itemOffers,
				heartContainerOffers,
				staminaVesselOffers,
				essenceOffers,
				userTags);
	}

	@NotNull protected abstract T instantiate(@NotNull ResourceLocation recipeId,
	                                          @NotNull JsonObject json,
	                                          @NotNull ResourceLocation bargainType,
	                                          @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
	                                          int heartContainerDemands,
	                                          int staminaVesselDemands,
	                                          int essenceDemands,
	                                          @NotNull List<@NotNull QuantifiedItem> itemOffers,
	                                          int heartContainerOffers,
	                                          int staminaVesselOffers,
	                                          int essenceOffers,
	                                          @NotNull Set<@NotNull String> userTags);

	@Override @NotNull public T fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer){
		ResourceLocation bargainType = buffer.readResourceLocation();

		List<QuantifiedIngredient> itemDemands = new ArrayList<>();
		for(int i = 0, size = buffer.readVarInt(); i<size; i++)
			itemDemands.add(QuantifiedIngredient.read(buffer));
		int heartContainerDemands = buffer.readVarInt();
		int staminaVesselDemands = buffer.readVarInt();
		int essenceDemands = buffer.readVarInt();

		List<QuantifiedItem> itemOffers = new ArrayList<>();
		for(int i = 0, size = buffer.readVarInt(); i<size; i++)
			itemOffers.add(QuantifiedItem.read(buffer));

		int heartContainerOffers = buffer.readVarInt();
		int staminaVesselOffers = buffer.readVarInt();
		int essenceOffers = buffer.readVarInt();

		Set<String> userTags = new ObjectOpenHashSet<>();
		for(int i = 0, size = buffer.readVarInt(); i<size; i++) userTags.add(buffer.readUtf());

		return instantiate(
				recipeId,
				buffer,
				bargainType,
				itemDemands,
				heartContainerDemands,
				staminaVesselDemands,
				essenceDemands,
				itemOffers,
				heartContainerOffers,
				staminaVesselOffers,
				essenceOffers,
				userTags);
	}

	@NotNull protected abstract T instantiate(@NotNull ResourceLocation recipeId,
	                                          @NotNull FriendlyByteBuf buffer,
	                                          @NotNull ResourceLocation bargainType,
	                                          @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
	                                          int heartContainerDemands,
	                                          int staminaVesselDemands,
	                                          int essenceDemands,
	                                          @NotNull List<@NotNull QuantifiedItem> itemOffers,
	                                          int heartContainerOffers,
	                                          int staminaVesselOffers,
	                                          int essenceOffers,
	                                          @NotNull Set<@NotNull String> userTags);

	@Override public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull T recipe){
		buffer.writeResourceLocation(recipe.getBargainType());

		List<QuantifiedIngredient> itemDemands = recipe.getItemDemands();
		buffer.writeVarInt(itemDemands.size());
		for(QuantifiedIngredient demand : itemDemands)
			demand.write(buffer);
		buffer.writeVarInt(recipe.getHeartContainerDemands());
		buffer.writeVarInt(recipe.getStaminaVesselDemands());
		buffer.writeVarInt(recipe.getEssenceDemands());

		List<QuantifiedItem> itemOffers = recipe.getItemOffers();
		buffer.writeVarInt(itemOffers.size());
		for(QuantifiedItem offer : itemOffers)
			offer.write(buffer);
		buffer.writeVarInt(recipe.getHeartContainerOffers());
		buffer.writeVarInt(recipe.getStaminaVesselOffers());
		buffer.writeVarInt(recipe.getEssenceOffers());

		Set<String> userTags = recipe.getUserTags();
		buffer.writeVarInt(userTags.size());
		for(String userTag : userTags){
			buffer.writeUtf(userTag);
		}
	}

	public static final class Simple extends SimpleBargainSerializer<SimpleBargain>{
		@Override @NotNull protected SimpleBargain instantiate(@NotNull ResourceLocation recipeId,
		                                                       @NotNull JsonObject json,
		                                                       @NotNull ResourceLocation bargainType,
		                                                       @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
		                                                       int heartContainerDemands,
		                                                       int staminaVesselDemands,
		                                                       int essenceDemands,
		                                                       @NotNull List<@NotNull QuantifiedItem> itemOffers,
		                                                       int heartContainerOffers,
		                                                       int staminaVesselOffers,
		                                                       int essenceOffers,
		                                                       @NotNull Set<@NotNull String> userTags){
			return new SimpleBargain(recipeId,
					bargainType,
					itemDemands,
					heartContainerDemands,
					staminaVesselDemands,
					essenceDemands,
					itemOffers,
					heartContainerOffers,
					staminaVesselOffers,
					essenceOffers,
					userTags);
		}

		@Override @NotNull protected SimpleBargain instantiate(@NotNull ResourceLocation recipeId,
		                                                       @NotNull FriendlyByteBuf buffer,
		                                                       @NotNull ResourceLocation bargainType,
		                                                       @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
		                                                       int heartContainerDemands,
		                                                       int staminaVesselDemands,
		                                                       int essenceDemands,
		                                                       @NotNull List<@NotNull QuantifiedItem> itemOffers,
		                                                       int heartContainerOffers,
		                                                       int staminaVesselOffers,
		                                                       int essenceOffers,
		                                                       @NotNull Set<@NotNull String> userTags){
			return new SimpleBargain(recipeId,
					bargainType,
					itemDemands,
					heartContainerDemands,
					staminaVesselDemands,
					essenceDemands,
					itemOffers,
					heartContainerOffers,
					staminaVesselOffers,
					essenceOffers,
					userTags);
		}
	}
}
