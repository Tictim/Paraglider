package tictim.paraglider.wind;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public record WindSource(@NotNull List<Condition> conditions, int height) {
	public static final int DEFAULT_HEIGHT = 10;

	public static final Codec<WindSource> CODEC = RecordCodecBuilder.create(b -> b.group(
			elementOrList(Condition.CODEC, true)
					.fieldOf("conditions").forGetter(WindSource::conditions),
			Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(WindSource::height)
	).apply(b, WindSource::new));

	private static <T> Codec<List<T>> elementOrList(Codec<T> t, boolean nonEmpty) {
		return elementOrList(t, nonEmpty ? t.listOf(1, Integer.MAX_VALUE) : t.listOf());
	}

	private static <T> Codec<List<T>> elementOrList(Codec<T> t, Codec<List<T>> tList) {
		return Codec.either(t, tList).xmap(
				l -> l.map(List::of, Function.identity()),
				l -> l.size() == 1 ? Either.left(l.getFirst()) : Either.right(l)
		);
	}

	public void check(
			@Nullable Consumer<@NotNull String> warnings,
			@Nullable Consumer<@NotNull String> errors) {
		if (this.conditions.isEmpty() && errors != null) {
			errors.accept("No conditions");
		}

		int index = 1;
		for (Condition condition : this.conditions) {
			condition.check(index++, warnings, errors);
		}

		if (this.height <= 0 && warnings != null) {
			warnings.accept("No height");
		}
	}

	public sealed interface Condition {
		// theres got to be a better way
		Codec<Condition> CODEC = Codec.xor(BlockStateCondition.CODEC, TagCondition.CODEC).xmap(
				e -> e.map(Function.identity(), Function.identity()),
				c -> switch (c) {
					case BlockStateCondition blockStateCondition -> Either.left(blockStateCondition);
					case TagCondition tagCondition -> Either.right(tagCondition);
				}
		);

		void check(int index, @Nullable Consumer<@NotNull String> warnings, @Nullable Consumer<@NotNull String> errors);
	}

	public record BlockStateCondition(
			List<Either<Block, Identifier>> block,
			List<StateProperty> properties
	) implements Condition {
		public static final Codec<BlockStateCondition> CODEC = RecordCodecBuilder.create(b -> b.group(
				elementOrList(
						Codec.either(BuiltInRegistries.BLOCK.byNameCodec(), Identifier.CODEC),
						true
				).fieldOf("block").forGetter(BlockStateCondition::block),
				Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
						m -> m.entrySet().stream()
								.map(e -> new StateProperty(e.getKey(), e.getValue()))
								.toList(),
						l -> l.stream().<Map<String, String>>collect(
								LinkedHashMap::new,
								(m, p) -> m.put(p.name, p.value),
								(m1, m2) -> {})
				).optionalFieldOf("properties", List.of()).forGetter(BlockStateCondition::properties)
		).apply(b, BlockStateCondition::new));

		@Override public void check(
				int index,
				@Nullable Consumer<@NotNull String> warnings,
				@Nullable Consumer<@NotNull String> errors) {
			if (this.block.isEmpty() && errors != null) {
				errors.accept("No blocks in condition #" + index);
			}
		}
	}

	public record StateProperty(String name, String value) {}

	public record TagCondition(List<TagKey<Block>> tags) implements Condition {
		public static final Codec<TagCondition> CODEC = TagKey.codec(Registries.BLOCK)
				.listOf(1, Integer.MAX_VALUE)
				.xmap(TagCondition::new, TagCondition::tags)
				.fieldOf("tag")
				.codec();

		@Override public void check(
				int index,
				@Nullable Consumer<@NotNull String> warnings,
				@Nullable Consumer<@NotNull String> errors) {
			if (this.tags.isEmpty() && errors != null) {
				errors.accept("No tags in condition #" + index);
			}
		}
	}
}
