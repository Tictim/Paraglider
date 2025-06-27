package tictim.paraglider.wind;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.config.DebugCfg;

import java.util.*;
import java.util.stream.Collectors;

public class WindSourceRegistry {
	public static final ResourceKey<Registry<WindSource>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ParagliderAPI.id("wind_sources"));

	private static final Logger LOGGER = LogManager.getLogger("Paraglider - WindSourceRegistry");

	public static @NotNull WindSourceRegistry get() {
		return ParagliderMod.instance().windSourceRegistry();
	}

	private WindSources windSources = new WindSources(Map.of(), 0);

	public int maxWindHeight() {
		return this.windSources.maxWindHeight;
	}

	public int getWindSourceHeight(@NotNull BlockState state) {
		var map = this.windSources.map.get(state.getBlock());
		if (map != null) return map.getInt(state);
		else return 0;
	}

	public record WindSources(
			Map<Block, Object2IntOpenHashMap<BlockState>> map,
			int maxWindHeight
	) {}

	public static final class ReloadListener extends SimplePreparableReloadListener<Void> {
		private final WindSourceRegistry windSourceRegistry;
		private final RegistryAccess registryAccess;

		private final Map<Block, Object2IntOpenHashMap<BlockState>> windSources = new HashMap<>();
		private int maxWindHeight = 0;

		public ReloadListener(WindSourceRegistry windSourceRegistry, RegistryAccess registryAccess) {
			this.windSourceRegistry = windSourceRegistry;
			this.registryAccess = registryAccess;
		}

		@Override protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
			return null;
		}

		@Override protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
			readWindSources();
			this.windSourceRegistry.windSources = new WindSources(this.windSources, this.maxWindHeight);
		}

		private void readWindSources() {
			boolean verbose = DebugCfg.get().verboseWindSourceLoading();

			Registry<Block> blocks = this.registryAccess.lookupOrThrow(Registries.BLOCK);
			Registry<WindSource> windSourceReg = this.registryAccess.lookupOrThrow(REGISTRY_KEY);
			List<Block> blockCache = new ArrayList<>();
			List<BlockState> blockStateCache = new ArrayList<>();

			if (verbose) LOGGER.info("Loading {} wind sources", windSourceReg.size());

			for (Holder<WindSource> holder : windSourceReg.asHolderIdMap()) {
				WindSource wind = holder.value();

				for (int i = 0; i < wind.conditions().size(); i++) {
					WindSource.Condition condition = wind.conditions().get(i);
					switch (condition) {
						case WindSource.BlockStateCondition blockStateCondition -> {
							for (Either<Block, ResourceLocation> e : blockStateCondition.block()) {
								e.ifLeft(blockCache::add);
								int finalI = i;
								e.ifRight(id -> LOGGER.warn("{}#{}:Cannot find block with ID {}", n(holder), finalI, id));
							}

							for (Block block : blockCache) {
								if (blockStateCondition.properties().isEmpty()) {
									add(wind.height(), block, null);
									continue;
								}

								blockStateCache.addAll(block.getStateDefinition().getPossibleStates());
								boolean error = false;

								for (WindSource.StateProperty property : blockStateCondition.properties()) {
									Property<?> p = block.getStateDefinition().getProperty(property.name());
									if (p == null) {
										LOGGER.warn("{}#{}: No property with name {} found for block {}; filtering all states",
												n(holder), i, property.name(), blockId(block));
										blockStateCache.clear();
										error = true;
										break;
									}

									Optional<?> v = p.getValue(property.value());
									if (v.isEmpty()) {
										LOGGER.warn("{}#{}: No value {} present in block {}, property {}; filtering all states",
												n(holder), i, property.value(), property.name(), blockId(block));
									}

									// equals is probably fine right??
									blockStateCache.removeIf(state -> !state.getOptionalValue(p).equals(v));
								}

								if (!error && blockStateCache.isEmpty()) {
									LOGGER.warn("{}#{}: All possible states filtered out for block {}",
											n(holder), i, blockId(block));
								}

								for (BlockState state : blockStateCache) {
									add(wind.height(), state.getBlock(), state);
								}

								blockStateCache.clear();
							}

							blockCache.clear();
						}
						case WindSource.TagCondition tagCondition -> {
							for (TagKey<Block> tag : tagCondition.tags()) {
								boolean found = false;
								for (Holder<Block> h : blocks.getTagOrEmpty(tag)) {
									found = true;
									add(wind.height(), h.value(), null);
								}

								if (!found) LOGGER.warn("{}#{}: Empty block tag {}",
										n(holder), i, tag.location());
							}
						}
					}
				}
			}

			if (verbose) {
				LOGGER.info("Loaded wind source list for {} blocks", this.windSources.size());
				for (var e : this.windSources.entrySet()) {
					Block block = e.getKey();
					ResourceLocation blockId = blockId(block);
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

		private static <T> String n(Holder<T> holder) {
			return holder.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse("[No ID]");
		}

		private static ResourceLocation blockId(Block block) {
			return BuiltInRegistries.BLOCK.getKey(block);
		}

		private static <T extends Comparable<T>> String propertyValueToString(BlockState state, Property<T> property) {
			return property.getName(state.getValue(property));
		}

		private void add(int height, @NotNull Block block, @Nullable BlockState state) {
			var m = this.windSources.computeIfAbsent(block, b -> new Object2IntOpenHashMap<>());
			if (state == null) {
				int d = m.defaultReturnValue();
				if (d < height) m.defaultReturnValue(height);
			} else {
				m.mergeInt(state, height, Integer::max);
			}

			this.maxWindHeight = Math.max(this.maxWindHeight, height);
		}
	}
}
