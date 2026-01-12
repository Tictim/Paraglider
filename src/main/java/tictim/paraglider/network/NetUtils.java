package tictim.paraglider.network;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import tictim.paraglider.api.bargain.BargainPreview;

public final class NetUtils {
	private NetUtils() {}

	public static final StreamCodec<FriendlyByteBuf, IntList> INT_LIST =
			StreamCodec.of(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray)
					.map(IntArrayList::new, IntCollection::toIntArray);

	public static final StreamCodec<RegistryFriendlyByteBuf, BargainPreview<?>> BARGAIN_PREVIEW = StreamCodec.of(
			NetUtils::encodeBargainPreview, NetUtils::decodeBargainPreview);

	@SuppressWarnings("unchecked")
	private static <T extends BargainPreview<T>> void encodeBargainPreview(RegistryFriendlyByteBuf buffer, BargainPreview<T> preview) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.lookupOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		var type = preview.type();
		int id = bargainPreviewTypes.getId(type);
		if (id == -1) throw new IllegalArgumentException("Unknown bargain preview type: " + type);
		buffer.writeVarInt(id);
		type.streamCodec().encode(buffer, (T) preview);
	}

	private static BargainPreview<?> decodeBargainPreview(RegistryFriendlyByteBuf buffer) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.lookupOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		return bargainPreviewTypes.get(buffer.readVarInt())
				.map(ref -> ref.value().streamCodec().decode(buffer))
				.orElseThrow();
	}
}
