package tictim.paraglider.contents;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.config.FeatureCfg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public final class ParagliderVillageStructures{
	private ParagliderVillageStructures(){}

	public static void addVillageStructures(@NotNull MinecraftServer server){
		if(!FeatureCfg.get().enableStructures()) return;

		Registry<StructureTemplatePool> reg = server.registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();

		ParagliderMod.LOGGER.debug("Start adding village structures");
		appendPool(reg, new ResourceLocation("village/desert/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":gerudo_village_goddess_statue"), 1),
				Pair.of(StructurePoolElement.legacy(MODID+":desert_village_horned_statue"), 1)
		);
		appendPool(reg, new ResourceLocation("village/plains/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":hateno_village_goddess_statue"), 1),
				Pair.of(StructurePoolElement.legacy(MODID+":plains_village_horned_statue"), 1)
		);
		appendPool(reg, new ResourceLocation("village/savanna/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":rito_village_goddess_statue"), 3),
				Pair.of(StructurePoolElement.legacy(MODID+":savanna_village_horned_statue"), 1)
		);
		appendPool(reg, new ResourceLocation("village/snowy/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":snowy_village_horned_statue"), 1)
		);
		appendPool(reg, new ResourceLocation("village/taiga/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":kakariko_village_goddess_statue"), 3),
				Pair.of(StructurePoolElement.legacy(MODID+":taiga_village_horned_statue"), 1)
		);
		ParagliderMod.LOGGER.debug("Finished adding village structures");
	}

	@SafeVarargs
	private static void appendPool(
			Registry<StructureTemplatePool> templatePoolRegistry,
			ResourceLocation id,
			Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>... elementToWeight){
		StructureTemplatePool pool = templatePoolRegistry.get(id);
		if(pool==null){
			ParagliderMod.LOGGER.warn("Template pool '{}' doesn't exist", id);
			return;
		}

		if(elementToWeight.length==0) return;

		List<Pair<StructurePoolElement, Integer>> newWeightedPool = new ArrayList<>(pool.rawTemplates.size()+elementToWeight.length);
		newWeightedPool.addAll(pool.rawTemplates);
		for(Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> p : elementToWeight)
			newWeightedPool.add(Pair.of(p.getFirst().apply(StructureTemplatePool.Projection.RIGID), p.getSecond()));
		pool.rawTemplates = newWeightedPool;

		pool.templates.clear();
		for(Pair<StructurePoolElement, Integer> pair : pool.rawTemplates){
			StructurePoolElement structurepoolelement = pair.getFirst();

			for(int i = 0; i<pair.getSecond(); ++i){
				pool.templates.add(structurepoolelement);
			}
		}

		ParagliderMod.LOGGER.debug("Added {} elements to template pool '{}'", elementToWeight.length, id);
	}
}
