package datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class Datagen{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event){
		DataGenerator gen = event.getGenerator();
		if(event.includeServer()){
			gen.addProvider(new RecipeGen(gen));
			BlockTagGen blockTagGen = new BlockTagGen(gen, event.getExistingFileHelper());
			gen.addProvider(blockTagGen);
			gen.addProvider(new ItemTagGen(gen, blockTagGen, event.getExistingFileHelper()));
			gen.addProvider(new BiomeTagGen(gen, event.getExistingFileHelper()));
			gen.addProvider(new LootTableGen(gen));
			gen.addProvider(new LootModifierProvider(gen, MODID));
			gen.addProvider(new AdvancementGen(gen, event.getExistingFileHelper()));
		}
	}
}
