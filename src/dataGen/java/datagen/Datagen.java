package datagen;

import datagen.test.TestRecipeGen;
import datagen.test.TestWindSourceGen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import tictim.paraglider.ParagliderMod;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
public class Datagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createProvider(ModelGen::new);

		event.createProvider(RecipeGen.Runner::new);
		event.createBlockAndItemTags(BlockTagGen::new, ItemTagGen::new);
		event.createProvider(BiomeTagGen::new);
		event.createProvider(LootTableGen::new);
		event.createProvider(LootModifierGen::new);
		event.createProvider(AdvancementGen::new);
		event.createProvider(BargainTypeGen::new);
		event.createProvider(WindSourceGen::new);
		event.createProvider(DatapackEntryGen::new);

		String paragliderTestDatagen = System.getenv("PARAGLIDER_TEST_DATAGEN");
		if (paragliderTestDatagen != null && !paragliderTestDatagen.isEmpty()) {
			ParagliderMod.LOGGER.info("Generating test datagen");

			event.createProvider(TestRecipeGen.Runner::new);
			event.createProvider(TestWindSourceGen::new);
		}
	}
}
