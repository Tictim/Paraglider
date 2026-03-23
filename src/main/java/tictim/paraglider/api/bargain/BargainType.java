package tictim.paraglider.api.bargain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record BargainType(BargainDialog dialog) {
	public static final Codec<BargainType> CODEC = RecordCodecBuilder.create(b -> b.group(
			BargainDialog.CODEC.optionalFieldOf("dialog", BargainDialog.EMPTY).forGetter(BargainType::dialog)
	).apply(b, BargainType::new));
}
