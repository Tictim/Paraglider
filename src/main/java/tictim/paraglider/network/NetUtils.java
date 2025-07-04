package tictim.paraglider.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.bargain.BargainPreview;

public final class NetUtils {
	private NetUtils() {}

	public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3 = StreamCodec.of(
			FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);

	public static final StreamCodec<FriendlyByteBuf, IntList> INT_LIST =
			StreamCodec.of(FriendlyByteBuf::writeVarIntArray, FriendlyByteBuf::readVarIntArray)
					.map(IntArrayList::new, IntCollection::toIntArray);

	public static final StreamCodec<RegistryFriendlyByteBuf, BargainPreview<?>> BARGAIN_PREVIEW = StreamCodec.of(
			NetUtils::encodeBargainPreview, NetUtils::decodeBargainPreview);

	@SuppressWarnings("unchecked")
	private static <T extends BargainPreview<T>> void encodeBargainPreview(RegistryFriendlyByteBuf buffer, BargainPreview<T> preview) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.registryOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		var type = preview.type();
		int id = bargainPreviewTypes.getId(type);
		if (id == -1) throw new IllegalArgumentException("Unknown bargain preview type: " + type);
		buffer.writeVarInt(id);
		type.streamCodec().encode(buffer, (T)preview);
	}

	private static BargainPreview<?> decodeBargainPreview(RegistryFriendlyByteBuf buffer) {
		Registry<BargainPreview.Type<?>> bargainPreviewTypes = buffer.registryAccess()
				.registryOrThrow(BargainPreview.TYPE_REGISTRY_KEY);

		return bargainPreviewTypes.getHolder(buffer.readVarInt())
				.map(ref -> ref.value().streamCodec().decode(buffer))
				.orElseThrow();
	}

	@SuppressWarnings("DataFlowIssue")
	public static <B extends ByteBuf, V> StreamCodec<B, @Nullable V> nullable(StreamCodec<? super B, V> streamCodec) {
		return StreamCodec.of((buf, val) -> {
			if (val == null) buf.writeBoolean(false);
			else {
				buf.writeBoolean(true);
				streamCodec.encode(buf, val);
			}
		}, buf -> {
			if (buf.readBoolean()) return streamCodec.decode(buf);
			else return null;
		});
	}
}
