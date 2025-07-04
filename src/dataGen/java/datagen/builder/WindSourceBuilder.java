package datagen.builder;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.wind.WindSource;

import java.util.*;
import java.util.function.Consumer;

public class WindSourceBuilder {
	private final List<WindSource.Condition> conditions = new ArrayList<>();
	private int height = WindSource.DEFAULT_HEIGHT;

	public WindSourceBuilder condition(WindSource.Condition condition) {
		this.conditions.add(condition);
		return this;
	}

	public WindSourceBuilder blocks(Block... blocks) {
		return blockStates(blocks, null);
	}

	public WindSourceBuilder blockStates(Consumer<PropertyBuilder> propertyBuilder, Block... blocks) {
		PropertyBuilder pb = new PropertyBuilder(blocks);
		propertyBuilder.accept(pb);
		return blockStates(blocks, pb.build());
	}

	private WindSourceBuilder blockStates(Block[] blocks, @Nullable List<WindSource.StateProperty> properties) {
		return condition(new WindSource.BlockStateCondition(
				Arrays.stream(blocks).<Either<Block, ResourceLocation>>map(Either::left).toList(),
				properties != null ? properties : List.of()
		));
	}

	@SafeVarargs public final WindSourceBuilder tags(TagKey<Block>... blocks) {
		return condition(new WindSource.TagCondition(List.of(blocks)));
	}

	public WindSourceBuilder height(int height) {
		this.height = height;
		return this;
	}

	public WindSource build() {
		return new WindSource(this.conditions, this.height);
	}

	public void save(JsonCodecProvider<WindSource> provider, ResourceLocation id) {
		boolean[] error = {false};
		WindSource windSource = build();

		windSource.check(
				s -> ParagliderMod.LOGGER.warn("WindSource {}: {}", id, s),
				s -> {
					ParagliderMod.LOGGER.error("WindSource {}: {}", id, s);
					error[0] = true;
				});

		if (error[0]) {
			throw new IllegalStateException("Wind source validation failed for entry " + id + ", see log for details");
		}

		provider.unconditional(id, windSource);
	}

	public static class PropertyBuilder {
		private final Block[] blocks;
		private final Map<String, String> props = new LinkedHashMap<>();

		public PropertyBuilder(Block... blocks) {
			this.blocks = blocks;
		}

		public <T extends Comparable<T>> PropertyBuilder property(Property<T> property, T value) {
			String name = property.getName(value);
			if (property.getValue(name).isEmpty())
				throw new IllegalArgumentException("Invalid value " + value + " for block state property " + property);

			if (this.props.containsKey(property.getName())) {
				throw new IllegalStateException("Trying to add property with same name twice: " + property.getName());
			}

			for (Block block : this.blocks) {
				Property<?> blockProperty = block.getStateDefinition().getProperty(property.getName());
				if (blockProperty == null) {
					throw new IllegalArgumentException("Property " + property.getName() + " not found for block " + BuiltInRegistries.BLOCK.getKey(block));
				} else if (blockProperty != property) {
					throw new IllegalArgumentException("Property instance with name " + property.getName() + " differs on block " + BuiltInRegistries.BLOCK.getKey(block));
				}
			}

			this.props.put(property.getName(), name);
			return this;
		}

		public PropertyBuilder uncheckedProperty(String property, String value) {
			if (this.props.putIfAbsent(property, value) != null) {
				throw new IllegalStateException("Trying to add property with same name twice: " + property);
			}
			return this;
		}

		List<WindSource.StateProperty> build() {
			if (this.props.isEmpty()) throw new IllegalStateException("No property conditions");
			return this.props.entrySet().stream()
					.map(e -> new WindSource.StateProperty(e.getKey(), e.getValue()))
					.toList();
		}
	}
}
