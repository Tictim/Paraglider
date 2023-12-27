package tictim.paraglider.fabric.contents.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.contents.recipe.SimpleBargain;
import tictim.paraglider.contents.recipe.SimpleBargainSerializer;

import java.util.Set;

public class FabricBargainSerializer extends SimpleBargainSerializer<FabricBargain>{
	private final Codec<FabricBargain> codec = RecordCodecBuilder.create(instance -> commonFields(instance)
		.and(instance.group(
			Codec.BOOL.fieldOf("usesHeartContainerFeature").orElse(false).forGetter(FabricBargain::isUsesHeartContainerFeature),
			Codec.BOOL.fieldOf("usesStaminaVesselFeature").orElse(false).forGetter(FabricBargain::isUsesStaminaVesselFeature))
		).apply(instance, FabricBargain::new)
	);

	@Override @NotNull public Codec<FabricBargain> codec() {
		return codec;
	}

	// Recipe constructed from packets don't need any special treatment, since the two boolean flags are only used on server side
	@Override @NotNull protected FabricBargain instantiate(
	                                                       @NotNull FriendlyByteBuf buffer,
	                                                       @NotNull ResourceLocation bargainType,
	                                                       @NotNull SimpleBargain.Demand demand,
	                                                       @NotNull SimpleBargain.Offer offer,
	                                                       @NotNull Set<@NotNull String> userTags){
		return new FabricBargain(bargainType, demand, offer, userTags,
				false, false);
	}
}
