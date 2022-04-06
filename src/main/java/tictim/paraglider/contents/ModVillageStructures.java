package tictim.paraglider.contents;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModVillageStructures{
	private ModVillageStructures(){}

	public static void addVillageStructures(){
		if(!ModCfg.enableStructures()) return;
		ParagliderMod.LOGGER.debug("Start adding village structures");
		appendPool(new ResourceLocation("village/desert/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":gerudo_village_goddess_statue"), 1),
				Pair.of(StructurePoolElement.legacy(MODID+":desert_village_horned_statue"), 1)
		);
		appendPool(new ResourceLocation("village/plains/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":hateno_village_goddess_statue"), 1),
				Pair.of(StructurePoolElement.legacy(MODID+":plains_village_horned_statue"), 1)
		);
		appendPool(new ResourceLocation("village/savanna/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":rito_village_goddess_statue"), 3),
				Pair.of(StructurePoolElement.legacy(MODID+":savanna_village_horned_statue"), 1)
		);
		appendPool(new ResourceLocation("village/snowy/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":snowy_village_horned_statue"), 1)
		);
		appendPool(new ResourceLocation("village/taiga/houses"),
				Pair.of(StructurePoolElement.legacy(MODID+":kakariko_village_goddess_statue"), 3),
				Pair.of(StructurePoolElement.legacy(MODID+":taiga_village_horned_statue"), 1)
		);
		ParagliderMod.LOGGER.debug("Finished adding village structures");
	}

	@SafeVarargs
	private static void appendPool(ResourceLocation pool, Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>... elementToWeight){
		StructureTemplatePool old = BuiltinRegistries.TEMPLATE_POOL.get(pool);
		if(old==null||old==Pools.bootstrap().value()){
			ParagliderMod.LOGGER.warn("Template pool '{}' doesn't exists", pool);
			return;
		}

		if(elementToWeight.length==0) return;

		List<Pair<StructurePoolElement, Integer>> newWeightedPool = new ArrayList<>(old.rawTemplates.size()+elementToWeight.length);
		newWeightedPool.addAll(old.rawTemplates);
		for(Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer> p : elementToWeight)
			newWeightedPool.add(Pair.of(p.getFirst().apply(StructureTemplatePool.Projection.RIGID), p.getSecond()));

		((WritableRegistry<StructureTemplatePool>)BuiltinRegistries.TEMPLATE_POOL).registerOrOverride(
				OptionalInt.of(BuiltinRegistries.TEMPLATE_POOL.getId(old)),
				ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, pool),
				new StructureTemplatePool(pool, old.getName(), newWeightedPool),
				Lifecycle.stable());

		ParagliderMod.LOGGER.debug("Added {} elements to template pool '{}'", elementToWeight.length, pool);
	}
}
