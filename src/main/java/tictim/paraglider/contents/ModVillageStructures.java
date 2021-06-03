package tictim.paraglider.contents;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import tictim.paraglider.ParagliderMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModVillageStructures{
	private ModVillageStructures(){}

	public static void addVillageStructures(){
		ParagliderMod.LOGGER.debug("Start adding village structures");
		appendPool(new ResourceLocation("village/desert/houses"), a -> {
			a.append(JigsawPiece.func_242849_a(MODID+":gerudo_village_goddess_statue"), 1);
			a.append(JigsawPiece.func_242849_a(MODID+":desert_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/plains/houses"), a -> {
			a.append(JigsawPiece.func_242849_a(MODID+":hateno_village_goddess_statue"), 1);
			a.append(JigsawPiece.func_242849_a(MODID+":plains_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/savanna/houses"), a -> {
			a.append(JigsawPiece.func_242849_a(MODID+":rito_village_goddess_statue"), 3);
			a.append(JigsawPiece.func_242849_a(MODID+":savanna_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/snowy/houses"), a -> {
			a.append(JigsawPiece.func_242849_a(MODID+":snowy_village_horned_statue"), 1);
		});
		appendPool(new ResourceLocation("village/taiga/houses"), a -> {
			a.append(JigsawPiece.func_242849_a(MODID+":kakariko_village_goddess_statue"), 3);
			a.append(JigsawPiece.func_242849_a(MODID+":taiga_village_horned_statue"), 1);
		});
		ParagliderMod.LOGGER.debug("Finished adding village structures");
	}

	private static void appendPool(ResourceLocation pool, Consumer<PoolAppender> c){
		JigsawPattern old = WorldGenRegistries.JIGSAW_POOL.getOrDefault(pool);
		if(old==null||old==JigsawPatternRegistry.func_244093_a()){
			ParagliderMod.LOGGER.warn("Jigsaw pool '{}' doesn't exists", pool);
			return;
		}

		PoolAppender appender = new PoolAppender();
		c.accept(appender);

		if(appender.structureToWeight.isEmpty()) return;

		List<Pair<JigsawPiece, Integer>> newWeightedPool = new ArrayList<>();
		newWeightedPool.addAll(old.rawTemplates);
		newWeightedPool.addAll(appender.structureToWeight);

		Registry.register(WorldGenRegistries.JIGSAW_POOL, pool, new JigsawPattern(pool, old.getName(), newWeightedPool));
		ParagliderMod.LOGGER.debug("Added {} elements to jigsaw pool '{}'", appender.structureToWeight.size(), pool);
	}

	private static final class PoolAppender{
		private final List<Pair<JigsawPiece, Integer>> structureToWeight = new ArrayList<>();

		public void append(Function<JigsawPattern.PlacementBehaviour, ? extends JigsawPiece> factory, int weight){
			structureToWeight.add(new Pair<>(factory.apply(JigsawPattern.PlacementBehaviour.RIGID), weight));
		}
	}
}
