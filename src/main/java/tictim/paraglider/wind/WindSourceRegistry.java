package tictim.paraglider.wind;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;

@NullMarked
public class WindSourceRegistry {
	public static final ResourceKey<Registry<WindSource>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ParagliderAPI.id("wind_sources"));

	private static final Logger LOGGER = LogManager.getLogger("Paraglider - WindSourceRegistry");

	public static WindSourceRegistry get() {
		return ParagliderMod.instance().windSourceRegistry();
	}

	private WindSourceBlockState blockStates = new WindSourceBlockState();

	public int maxWindHeight() {
		return this.blockStates.maxWindHeight();
	}

	public int getWindSourceHeight(BlockState state) {
		return this.blockStates.getWindSourceHeight(state);
	}

	public void computeWindSource(HolderLookup.Provider registryAccess) {
		this.blockStates = new WindSourceBlockState(registryAccess);
	}

	public static final class ReloadListener extends SimplePreparableReloadListener<Void> {
		private final WindSourceRegistry windSourceRegistry;

		public ReloadListener(WindSourceRegistry windSourceRegistry) {
			this.windSourceRegistry = windSourceRegistry;
		}

		@Override protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
			return null;
		}

		@Override protected void apply(Void preparations, ResourceManager manager, ProfilerFiller profiler) {
			this.windSourceRegistry.computeWindSource(getRegistryLookup());
		}
	}
}
