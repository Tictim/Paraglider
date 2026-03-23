package tictim.paraglider.contents.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.network.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@NullMarked
public class SimpleBargainCodec {
	private SimpleBargainCodec() {}

	public static final MapCodec<SimpleBargain> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			Codec.mapEither(Identifier.CODEC.fieldOf("bargainType"), Identifier.CODEC.fieldOf("owner"))
					.xmap(
							e -> e.map(Function.identity(), Function.identity()),
							Either::left)
					.forGetter(SimpleBargain::getBargainType),
			DemandComponent.CODEC.codec().optionalFieldOf("demands", DemandComponent.DEFAULT).forGetter(r ->
					new DemandComponent(r.getItemDemands(), r.getHeartContainerDemands(), r.getStaminaVesselDemands(), r.getEssenceDemands())),
			OfferComponent.CODEC.codec().optionalFieldOf("offers", OfferComponent.DEFAULT).forGetter(r ->
					new OfferComponent(r.getItemOffers(), r.getHeartContainerOffers(), r.getStaminaVesselOffers(), r.getEssenceOffers())),
			Codec.STRING.listOf().optionalFieldOf("tags", List.of())
					.xmap(Set::copyOf, List::copyOf)
					.forGetter(SimpleBargain::getUserTags)
	).apply(b, (bargainType, demands, offers, tags) -> new SimpleBargain(bargainType,
			demands.items, demands.heartContainers, demands.staminaVessels, demands.essences,
			offers.items, offers.heartContainers, offers.staminaVessels, offers.essences,
			tags)));

	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleBargain> STREAM_CODEC = StreamCodec.of(
			SimpleBargainCodec::toNetwork, SimpleBargainCodec::fromNetwork
	);

	private static SimpleBargain fromNetwork(RegistryFriendlyByteBuf buffer) {
		Identifier bargainType = buffer.readIdentifier();

		List<SizedIngredient> itemDemands = new ArrayList<>();
		for (int i = 0, size = buffer.readVarInt(); i < size; i++)
			itemDemands.add(SizedIngredient.STREAM_CODEC.decode(buffer));
		int heartContainerDemands = buffer.readVarInt();
		int staminaVesselDemands = buffer.readVarInt();
		int essenceDemands = buffer.readVarInt();

		List<ItemStackTemplate> itemOffers = new ArrayList<>();
		for (int i = 0, size = buffer.readVarInt(); i < size; i++)
			itemOffers.add(ItemStackTemplate.STREAM_CODEC.decode(buffer));

		int heartContainerOffers = buffer.readVarInt();
		int staminaVesselOffers = buffer.readVarInt();
		int essenceOffers = buffer.readVarInt();

		Set<String> userTags = new ObjectOpenHashSet<>();
		for (int i = 0, size = buffer.readVarInt(); i < size; i++) userTags.add(buffer.readUtf());

		return new SimpleBargain(
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

	private static void toNetwork(RegistryFriendlyByteBuf buffer, SimpleBargain recipe) {
		buffer.writeIdentifier(recipe.getBargainType());

		List<SizedIngredient> itemDemands = recipe.getItemDemands();
		buffer.writeVarInt(itemDemands.size());
		for (SizedIngredient demand : itemDemands)
			SizedIngredient.STREAM_CODEC.encode(buffer, demand);
		buffer.writeVarInt(recipe.getHeartContainerDemands());
		buffer.writeVarInt(recipe.getStaminaVesselDemands());
		buffer.writeVarInt(recipe.getEssenceDemands());

		List<ItemStackTemplate> itemOffers = recipe.getItemOffers();
		buffer.writeVarInt(itemOffers.size());
		for (ItemStackTemplate offer : itemOffers)
			ItemStackTemplate.STREAM_CODEC.encode(buffer, offer);
		buffer.writeVarInt(recipe.getHeartContainerOffers());
		buffer.writeVarInt(recipe.getStaminaVesselOffers());
		buffer.writeVarInt(recipe.getEssenceOffers());

		Set<String> userTags = recipe.getUserTags();
		buffer.writeVarInt(userTags.size());
		for (String userTag : userTags) {
			buffer.writeUtf(userTag);
		}
	}

	private record DemandComponent(
			List<SizedIngredient> items,
			int heartContainers,
			int staminaVessels,
			int essences
	) {
		private static final DemandComponent DEFAULT = new DemandComponent(List.of(), 0, 0, 0);

		private static final MapCodec<DemandComponent> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
				SizedIngredient.NESTED_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(DemandComponent::items),
				Codec.INT.optionalFieldOf("heartContainers", 0).forGetter(DemandComponent::heartContainers),
				Codec.INT.optionalFieldOf("staminaVessels", 0).forGetter(DemandComponent::staminaVessels),
				Codec.INT.optionalFieldOf("essences", 0).forGetter(DemandComponent::essences)
		).apply(b, DemandComponent::new));
	}

	private record OfferComponent(
			List<ItemStackTemplate> items,
			int heartContainers,
			int staminaVessels,
			int essences
	) {
		private static final OfferComponent DEFAULT = new OfferComponent(List.of(), 0, 0, 0);

		private static final MapCodec<OfferComponent> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
				NetUtils.ITEM_STACK_TEMPLATE_UNLIMITED_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(OfferComponent::items),
				Codec.INT.optionalFieldOf("heartContainers", 0).forGetter(OfferComponent::heartContainers),
				Codec.INT.optionalFieldOf("staminaVessels", 0).forGetter(OfferComponent::staminaVessels),
				Codec.INT.optionalFieldOf("essences", 0).forGetter(OfferComponent::essences)
		).apply(b, OfferComponent::new));
	}
}
