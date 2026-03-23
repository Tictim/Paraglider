package tictim.paraglider.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import tictim.paraglider.api.bargain.BargainPreview;

public final class NetUtils {
	private NetUtils() {}

	public static final StreamCodec<FriendlyByteBuf, IntList> INT_LIST =
			StreamCodec.of(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray)
					.map(IntArrayList::new, IntCollection::toIntArray);

	public static final StreamCodec<RegistryFriendlyByteBuf, BargainPreview<?>> BARGAIN_PREVIEW = StreamCodec.of(
			NetUtils::encodeBargainPreview, NetUtils::decodeBargainPreview);

	public static final MapCodec<ItemStackTemplate> ITEM_STACK_TEMPLATE_UNLIMITED_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Item.CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
			ExtraCodecs.intRange(1, Integer.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStackTemplate::count),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components)
	).apply(i, ItemStackTemplate::new));

	public static final Codec<ItemStackTemplate> ITEM_STACK_TEMPLATE_UNLIMITED_CODEC =
			Codec.withAlternative(ITEM_STACK_TEMPLATE_UNLIMITED_MAP_CODEC.codec(), Item.CODEC,
					item -> new ItemStackTemplate(item.value()));

	@SuppressWarnings("unchecked")
	private static <T extends BargainPreview<T>> void encodeBargainPreview(RegistryFriendlyByteBuf buffer, BargainPreview<T> preview) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.lookupOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		var type = preview.type();
		int id = bargainPreviewTypes.getId(type);
		if (id == -1) throw new IllegalArgumentException("Unknown bargain preview type: " + type);
		buffer.writeVarInt(id);
		type.streamCodec().encode(buffer, (T)preview);
	}

	private static BargainPreview<?> decodeBargainPreview(RegistryFriendlyByteBuf buffer) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.lookupOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		return bargainPreviewTypes.get(buffer.readVarInt())
				.map(ref -> ref.value().streamCodec().decode(buffer))
				.orElseThrow();
	}
}
