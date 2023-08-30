package tictim.paraglider.fabric.contents.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.recipe.SimpleBargain;

import java.util.List;
import java.util.Set;

/**
 * Sub-impl of bargain to disable bargain content if corresponding feature is turned off
 */
public class FabricBargain extends SimpleBargain{
	private final boolean usesHeartContainerFeature;
	private final boolean usesStaminaVesselFeature;

	public FabricBargain(@NotNull ResourceLocation id,
	                     @NotNull ResourceLocation bargainType,
	                     @NotNull List<@NotNull QuantifiedIngredient> itemDemands,
	                     int heartContainerDemands,
	                     int staminaVesselDemands,
	                     int essenceDemands,
	                     @NotNull List<@NotNull QuantifiedItem> itemOffers,
	                     int heartContainerOffers,
	                     int staminaVesselOffers,
	                     int essenceOffers,
	                     @NotNull Set<@NotNull String> userTags,
	                     boolean usesHeartContainerFeature,
	                     boolean usesStaminaVesselFeature){
		super(id, bargainType, itemDemands, heartContainerDemands, staminaVesselDemands, essenceDemands, itemOffers, heartContainerOffers, staminaVesselOffers, essenceOffers, userTags);
		this.usesHeartContainerFeature = usesHeartContainerFeature;
		this.usesStaminaVesselFeature = usesStaminaVesselFeature;
	}

	@Override public boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos){
		return (!usesHeartContainerFeature||FeatureCfg.get().enableHeartContainers())&&
				(!usesStaminaVesselFeature||FeatureCfg.get().enableStaminaVessels());
	}
}
