package tictim.paraglider.bargain;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@NullMarked
public class BargainRecipeChecker extends SimplePreparableReloadListener<Void> {
	private final RegistryAccess registryAccess;
	private final RecipeManager recipeManager;

	public BargainRecipeChecker(RegistryAccess registryAccess, RecipeManager recipeManager) {
		this.registryAccess = registryAccess;
		this.recipeManager = recipeManager;
	}

	@Override protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return null;
	}

	@Override protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<Identifier, List<RecipeHolder<Bargain>>> missingBargainTypes = new Object2ObjectAVLTreeMap<>();
		int count = 0;

		for (RecipeHolder<Bargain> b : this.recipeManager.recipeMap()
				.byType(Contents.get().bargainRecipeType())) {
			Identifier bargainType = b.value().getBargainType();
			if (BargainTypeRegistry.getFromID(this.registryAccess, Objects.requireNonNull(bargainType)) == null) {
				missingBargainTypes.computeIfAbsent(bargainType, s -> new ArrayList<>())
						.add(b);
				count++;
			}
		}

		if (count > 0) {
			ParagliderMod.LOGGER.error("Found {} issues in bargain recipes:\n  {}", count,
					missingBargainTypes.entrySet().stream()
							.map(e ->
									"Cannot resolve bargain type " + e.getKey() + " for bargain recipe(s) " +
											e.getValue().stream()
													.map(r -> r.id().identifier().toString())
													.collect(Collectors.joining(", ")))
							.collect(Collectors.joining("\n  ")));
		}
	}
}
