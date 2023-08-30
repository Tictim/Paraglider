package datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class Datagen{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		gen.addProvider(event.includeServer(), new RecipeGen(gen.getPackOutput()));
		BlockTagGen blockTagGen = new BlockTagGen(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
		gen.addProvider(event.includeServer(), blockTagGen);
		gen.addProvider(event.includeServer(), new ItemTagGen(gen.getPackOutput(), event.getLookupProvider(), blockTagGen.contentsGetter(), event.getExistingFileHelper()));
		gen.addProvider(event.includeServer(), new BiomeTagGen(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
		gen.addProvider(event.includeServer(), new LootTableGen(gen.getPackOutput()));
		gen.addProvider(event.includeServer(), new LootModifierProvider(gen.getPackOutput()));
		gen.addProvider(event.includeServer(), new AdvancementGen(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
		gen.addProvider(event.includeServer(), new BargainTypeGen(gen.getPackOutput(), event.getExistingFileHelper()));
	}
}
