package tictim.paraglider.fabric.contents.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;
import tictim.paraglider.contents.recipe.SimpleBargainSerializer;

import java.util.List;
import java.util.Set;

public class FabricBargainSerializer extends SimpleBargainSerializer<FabricBargain>{
	@Override @NotNull protected FabricBargain instantiate(@NotNull ResourceLocation recipeId,
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
		return new FabricBargain(recipeId, bargainType, itemDemands, heartContainerDemands, staminaVesselDemands,
				essenceDemands, itemOffers, heartContainerOffers, staminaVesselOffers, essenceOffers, userTags,
				GsonHelper.getAsBoolean(json, "usesHeartContainerFeature", false),
				GsonHelper.getAsBoolean(json, "usesStaminaVesselFeature", false));
	}

	// Recipe constructed from packets don't need any special treatment, since the two boolean flags are only used on server side
	@Override @NotNull protected FabricBargain instantiate(@NotNull ResourceLocation recipeId,
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
		return new FabricBargain(recipeId, bargainType, itemDemands, heartContainerDemands, staminaVesselDemands,
				essenceDemands, itemOffers, heartContainerOffers, staminaVesselOffers, essenceOffers, userTags,
				false, false);
	}
}
