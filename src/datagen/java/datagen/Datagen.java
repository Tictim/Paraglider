package datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class Datagen{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		gen.addProvider(event.includeServer(), new RecipeGen(gen));
		BlockTagGen blockTagGen = new BlockTagGen(gen, event.getExistingFileHelper());
		gen.addProvider(event.includeServer(), blockTagGen);
		gen.addProvider(event.includeServer(), new ItemTagGen(gen, blockTagGen, event.getExistingFileHelper()));
		gen.addProvider(event.includeServer(), new BiomeTagGen(gen, event.getExistingFileHelper()));
		gen.addProvider(event.includeServer(), new LootTableGen(gen));
		gen.addProvider(event.includeServer(), new LootModifierProvider(gen, MODID));
		gen.addProvider(event.includeServer(), new AdvancementGen(gen, event.getExistingFileHelper()));
	}
}
