package tictim.paraglider.contents;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModVillageStructures{
	private ModVillageStructures(){}

	public static void addVillageStructures(){
		if(!ModCfg.enableStructures()) return;
		ParagliderMod.LOGGER.debug("Start adding village structures");
		appendPool(new ResourceLocation("village/desert/houses"), a -> {
			a.append(StructurePoolElement.legacy(MODID+":gerudo_village_goddess_statue"), 1);
			a.append(StructurePoolElement.legacy(MODID+":desert_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/plains/houses"), a -> {
			a.append(StructurePoolElement.legacy(MODID+":hateno_village_goddess_statue"), 1);
			a.append(StructurePoolElement.legacy(MODID+":plains_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/savanna/houses"), a -> {
			a.append(StructurePoolElement.legacy(MODID+":rito_village_goddess_statue"), 3);
			a.append(StructurePoolElement.legacy(MODID+":savanna_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/snowy/houses"), a -> {
			a.append(StructurePoolElement.legacy(MODID+":snowy_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/taiga/houses"), a -> {
			a.append(StructurePoolElement.legacy(MODID+":kakariko_village_goddess_statue"), 3);
			a.append(StructurePoolElement.legacy(MODID+":taiga_village_horned_statue"), 1);
		});
		ParagliderMod.LOGGER.debug("Finished adding village structures");
	}

	private static void appendPool(ResourceLocation pool, Consumer<PoolAppender> c){
		StructureTemplatePool old = BuiltinRegistries.TEMPLATE_POOL.get(pool);
		if(old==null||old==Pools.bootstrap()){
			ParagliderMod.LOGGER.warn("Jigsaw pool '{}' doesn't exists", pool);
			return;
		}

		PoolAppender appender = new PoolAppender();
		c.accept(appender);

		if(appender.structureToWeight.isEmpty()) return;

		List<Pair<StructurePoolElement, Integer>> newWeightedPool = new ArrayList<>();
		newWeightedPool.addAll(old.rawTemplates);
		newWeightedPool.addAll(appender.structureToWeight);

		Registry.register(BuiltinRegistries.TEMPLATE_POOL, pool, new StructureTemplatePool(pool, old.getName(), newWeightedPool));
		ParagliderMod.LOGGER.debug("Added {} elements to jigsaw pool '{}'", appender.structureToWeight.size(), pool);
	}

	private static final class PoolAppender{
		private final List<Pair<StructurePoolElement, Integer>> structureToWeight = new ArrayList<>();

		public void append(Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> factory, int weight){
			structureToWeight.add(new Pair<>(factory.apply(StructureTemplatePool.Projection.RIGID), weight));
		}
	}
}
