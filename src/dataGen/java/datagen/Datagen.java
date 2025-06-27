package datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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
	}
}
