package datagen;

import datagen.test.TestRecipeGen;
import datagen.test.TestWindSourceGen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import tictim.paraglider.ParagliderMod;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@EventBusSubscriber(modid = MODID)
public class Datagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		event.createProvider(o -> new BlockStateGen(o, event.getExistingFileHelper()));
		event.createProvider(o -> new ItemModelGen(o, event.getExistingFileHelper()));

		event.createProvider(RecipeGen::new);
		event.createBlockAndItemTags(
				(o, p) -> new BlockTagGen(o, p, event.getExistingFileHelper()),
				(o, p, b) -> new ItemTagGen(o, p, b, event.getExistingFileHelper()));
		event.createProvider((o, p) -> new BiomeTagGen(o, p, event.getExistingFileHelper()));
		event.createProvider(LootTableGen::new);
		event.createProvider(LootModifierGen::new);
		event.createProvider((o, p) -> new AdvancementGen(o, p, event.getExistingFileHelper()));
		event.createProvider((o, p) -> new BargainTypeGen(o, p, event.getExistingFileHelper()));
		event.createProvider((o, p) -> new WindSourceGen(o, p, event.getExistingFileHelper()));
		event.createProvider(DatapackEntryGen::new);

		String paragliderTestDatagen = System.getenv("PARAGLIDER_TEST_DATAGEN");
		if (paragliderTestDatagen != null && !paragliderTestDatagen.isEmpty()) {
			ParagliderMod.LOGGER.info("Generating test datagen");

			event.createProvider((o, p) -> new TestRecipeGen(o, p).wrap());
			event.createProvider((o, p) -> new TestWindSourceGen(o, p, event.getExistingFileHelper()));
		}
	}
}
