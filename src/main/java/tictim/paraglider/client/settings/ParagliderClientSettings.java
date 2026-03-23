package tictim.paraglider.client.settings;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderClientMod;

import java.util.function.Function;

@NullMarked
public record ParagliderClientSettings(
		StaminaWheelPosition staminaWheelPosition,
		double windParticleFrequency,
		ExtraWheelAttachment extraWheelAttachment,
		boolean autoParagliding
) {
	public static final ParagliderClientSettings DEFAULT = new ParagliderClientSettings(
			StaminaWheelPosition.DEFAULT,
			1,
			ExtraWheelAttachment.LEFT,
			true
	);

	public static final Codec<ParagliderClientSettings> CODEC = RecordCodecBuilder.create(b -> b.group(
			Codec.xor(
							StaminaWheelPosition.ScreenProportion.CODEC.fieldOf("screen_proportion").codec(),
							StaminaWheelPosition.Anchored.CODEC.fieldOf("anchored").codec()
					).<StaminaWheelPosition>xmap(
							e -> e.map(Function.identity(), Function.identity()),
							r -> switch (r) {
								case StaminaWheelPosition.ScreenProportion p -> Either.left(p);
								case StaminaWheelPosition.Anchored a -> Either.right(a);
							})
					.optionalFieldOf("", StaminaWheelPosition.DEFAULT)
					.forGetter(ParagliderClientSettings::staminaWheelPosition),
			Codec.doubleRange(0, 1)
					.optionalFieldOf("wind_particle_frequency", 1.0)
					.forGetter(ParagliderClientSettings::windParticleFrequency),
			StringRepresentable.fromValues(ExtraWheelAttachment::values)
					.optionalFieldOf("extra_wheel_attachment", ExtraWheelAttachment.LEFT)
					.forGetter(ParagliderClientSettings::extraWheelAttachment),
			Codec.BOOL.optionalFieldOf("auto_paragliding", true)
					.forGetter(ParagliderClientSettings::autoParagliding)
	).apply(b, ParagliderClientSettings::new));

	public static ParagliderClientSettings get() {
		return ParagliderClientMod.instance().getSettings();
	}
}
