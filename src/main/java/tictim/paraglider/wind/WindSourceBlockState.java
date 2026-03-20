package tictim.paraglider.wind;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.config.DebugCfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static tictim.paraglider.ParagliderMod.LOGGER;

public class WindSourceBlockState {
	private final Map<Block, Object2IntOpenHashMap<BlockState>> blockStates = new Object2ObjectOpenHashMap<>();
	private int maxWindHeight;

	public WindSourceBlockState() {}

	public WindSourceBlockState(HolderLookup.Provider lookupProvider) {
		boolean verbose = DebugCfg.get().verboseWindSourceLoading();

		var blocks = lookupProvider.lookupOrThrow(Registries.BLOCK);
		var windSourceReg = lookupProvider.lookupOrThrow(WindSourceRegistry.REGISTRY_KEY);
		List<Block> blockCache = new ArrayList<>();
		List<BlockState> blockStateCache = new ArrayList<>();

		if (verbose) LOGGER.info("Loading wind sources");

		windSourceReg.listElements().forEach(ref -> {
			WindSource windSource = ref.value();

			for (int i = 0; i < windSource.conditions().size(); i++) {
				WindSource.Condition condition = windSource.conditions().get(i);
				switch (condition) {
					case WindSource.BlockStateCondition blockStateCondition -> {
						for (Either<Block, Identifier> e : blockStateCondition.block()) {
							e.ifLeft(blockCache::add);
							int finalI = i;
							e.ifRight(id -> LOGGER.warn("{}#{}:Cannot find block with ID {}", n(ref), finalI, id));
						}

						for (Block block : blockCache) {
							if (blockStateCondition.properties().isEmpty()) {
								add(windSource.height(), block, null);
								continue;
							}

							blockStateCache.addAll(block.getStateDefinition().getPossibleStates());
							boolean error = false;

							for (WindSource.StateProperty property : blockStateCondition.properties()) {
								Property<?> p = block.getStateDefinition().getProperty(property.name());
								if (p == null) {
									LOGGER.warn("{}#{}: No property with name {} found for block {}; filtering all states",
											n(ref), i, property.name(), blockId(block));
									blockStateCache.clear();
									error = true;
									break;
								}

								Optional<?> v = p.getValue(property.value());
								if (v.isEmpty()) {
									LOGGER.warn("{}#{}: No value {} present in block {}, property {}; filtering all states",
											n(ref), i, property.value(), property.name(), blockId(block));
								}

								// equals is probably fine right??
								blockStateCache.removeIf(state -> !state.getOptionalValue(p).equals(v));
							}

							if (!error && blockStateCache.isEmpty()) {
								LOGGER.warn("{}#{}: All possible states filtered out for block {}",
										n(ref), i, blockId(block));
							}

							for (BlockState state : blockStateCache) {
								add(windSource.height(), state.getBlock(), state);
							}

							blockStateCache.clear();
						}

						blockCache.clear();
					}
					case WindSource.TagCondition tagCondition -> {
						for (TagKey<@NotNull Block> tag : tagCondition.tags()) {
							blocks.get(tag).ifPresent(holders -> {
								for (Holder<@NotNull Block> h : holders) {
									add(windSource.height(), h.value(), null);
								}
							});
						}
					}
				}
			}
		});

		if (verbose) {
			LOGGER.info("Loaded wind source list for {} blocks", this.blockStates.size());
			for (var e : this.blockStates.entrySet()) {
				Block block = e.getKey();
				Identifier blockId = blockId(block);
				Object2IntOpenHashMap<BlockState> map = e.getValue();
				int defaultHeight = map.defaultReturnValue();

				if (map.isEmpty()) {
					LOGGER.info("{}: {}", blockId, defaultHeight);
				} else {
					LOGGER.info("{}:", blockId);
					for (var e2 : map.object2IntEntrySet()) {
						BlockState state = e2.getKey();
						LOGGER.info("  {}: {}", state.getProperties().stream()
										.map(p -> p.getName() + "=" + propertyValueToString(state, p))
										.collect(Collectors.joining(",")),
								e2.getIntValue());
					}
					if (defaultHeight > 0) LOGGER.info("  Fallback: {}", defaultHeight);
				}
			}
		}
	}

	private static <T> String n(Holder<@NotNull T> holder) {
		return holder.unwrapKey().map(ResourceKey::identifier).map(Identifier::toString).orElse("[No ID]");
	}

	private static Identifier blockId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	private static <T extends Comparable<T>> String propertyValueToString(BlockState state, Property<@NotNull T> property) {
		return property.getName(state.getValue(property));
	}

	private void add(int height, @NotNull Block block, @Nullable BlockState state) {
		var m = this.blockStates.computeIfAbsent(block, b -> new Object2IntOpenHashMap<>());
		if (state == null) {
			int d = m.defaultReturnValue();
			if (d < height) m.defaultReturnValue(height);
		} else {
			m.mergeInt(state, height, Integer::max);
		}

		this.maxWindHeight = Math.max(this.maxWindHeight, height);
	}

	public int getWindSourceHeight(@NotNull BlockState state) {
		var map = this.blockStates.get(state.getBlock());
		if (map != null) return map.getInt(state);
		else return 0;
	}

	public int maxWindHeight() {
		return maxWindHeight;
	}
}
