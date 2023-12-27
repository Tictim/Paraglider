package tictim.paraglider.fabric.contents.recipe;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.recipe.SimpleBargain;

import java.util.Set;

/**
 * Sub-impl of bargain to disable bargain content if corresponding feature is turned off
 */
@Getter
public class FabricBargain extends SimpleBargain{
	private final boolean usesHeartContainerFeature;
	private final boolean usesStaminaVesselFeature;

	public FabricBargain(@NotNull ResourceLocation bargainType,
	                     @NotNull Demand demand,
	                     @NotNull Offer offer,
	                     @NotNull Set<@NotNull String> userTags,
	                     boolean usesHeartContainerFeature,
	                     boolean usesStaminaVesselFeature){
		super(bargainType, demand, offer, userTags);
		this.usesHeartContainerFeature = usesHeartContainerFeature;
		this.usesStaminaVesselFeature = usesStaminaVesselFeature;
	}

	@Override public boolean isAvailableFor(@NotNull Player player, @Nullable BlockPos pos){
		return (!usesHeartContainerFeature||FeatureCfg.get().enableHeartContainers())&&
				(!usesStaminaVesselFeature||FeatureCfg.get().enableStaminaVessels());
	}
}
