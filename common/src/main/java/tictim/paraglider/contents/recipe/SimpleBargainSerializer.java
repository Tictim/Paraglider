package tictim.paraglider.contents.recipe;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.bargain.preview.QuantifiedIngredient;
import tictim.paraglider.bargain.preview.QuantifiedItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class SimpleBargainSerializer<T extends SimpleBargain> implements RecipeSerializer<T>{
	/**
	 * Aggregates the individual codec builders for the 4 common fields of {@link SimpleBargain} into a {@link Products.P4 product}.
	 *
	 * @param instance the {@link com.mojang.datafixers.kinds.Applicative} instance for {@link RecordCodecBuilder}
	 * @return an aggregation of the 4 fields
	 */
	@NotNull protected Products.P4<RecordCodecBuilder.Mu<T>, ResourceLocation, SimpleBargain.Demand, SimpleBargain.Offer, Set<String>> commonFields(RecordCodecBuilder.Instance<T> instance) {
		return instance.group(
			ResourceLocation.CODEC.fieldOf("bargainType").forGetter(SimpleBargain::getBargainType),
			SimpleBargain.Demand.CODEC.fieldOf("demands").forGetter(SimpleBargain::getDemand),
			SimpleBargain.Offer.CODEC.fieldOf("offers").forGetter(SimpleBargain::getOffer),
			Codec.STRING.listOf().fieldOf("tags")
				.xmap(c -> (Set<String>) new ObjectOpenHashSet<>(c), ArrayList::new)
				.forGetter(SimpleBargain::getUserTags)
		);
	}

	@Override @NotNull public T fromNetwork(@NotNull FriendlyByteBuf buffer){
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
				buffer,
				bargainType,
				new SimpleBargain.Demand(itemDemands,
					heartContainerDemands,
					staminaVesselDemands,
					essenceDemands),
				new SimpleBargain.Offer(itemOffers,
					heartContainerOffers,
					staminaVesselOffers,
					essenceOffers),
				userTags);
	}

	@NotNull protected abstract T instantiate(
	                                          @NotNull FriendlyByteBuf buffer,
	                                          @NotNull ResourceLocation bargainType,
	                                          @NotNull SimpleBargain.Demand demand,
	                                          @NotNull SimpleBargain.Offer offer,
	                                          @NotNull Set<@NotNull String> userTags);

	@Override public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull T recipe){
		buffer.writeResourceLocation(recipe.getBargainType());

		SimpleBargain.Demand demand = recipe.getDemand();
		List<QuantifiedIngredient> itemDemands = demand.items();
		buffer.writeVarInt(itemDemands.size());
		for (QuantifiedIngredient item : itemDemands)
			item.write(buffer);
		buffer.writeVarInt(demand.heartContainers());
		buffer.writeVarInt(demand.staminaVessels());
		buffer.writeVarInt(demand.essences());

		SimpleBargain.Offer offer = recipe.getOffer();
		List<QuantifiedItem> itemOffers = offer.items();
		buffer.writeVarInt(itemOffers.size());
		for (QuantifiedItem item : itemOffers)
			item.write(buffer);
		buffer.writeVarInt(offer.heartContainers());
		buffer.writeVarInt(offer.staminaVessels());
		buffer.writeVarInt(offer.essences());

		Set<String> userTags = recipe.getUserTags();
		buffer.writeVarInt(userTags.size());
		for(String userTag : userTags){
			buffer.writeUtf(userTag);
		}
	}

	public static final class Simple extends SimpleBargainSerializer<SimpleBargain>{
		private final Codec<SimpleBargain> codec = RecordCodecBuilder.create(instance -> commonFields(instance).apply(instance, SimpleBargain::new));

		@Override @NotNull public Codec<SimpleBargain> codec() {
			return codec;
		}

		@Override @NotNull protected SimpleBargain instantiate(
		                                                       @NotNull FriendlyByteBuf buffer,
		                                                       @NotNull ResourceLocation bargainType,
		                                                       @NotNull SimpleBargain.Demand demand,
		                                                       @NotNull SimpleBargain.Offer offer,
		                                                       @NotNull Set<@NotNull String> userTags){
			return new SimpleBargain(
					bargainType,
					demand,
					offer,
					userTags);
		}
	}
}
