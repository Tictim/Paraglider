package datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import tictim.paraglider.api.ParagliderAPI;

// TODO I cba to figure out how to configure gradle for defining common datagen code, so I'm copy pasting everything
@SuppressWarnings("unused")
public class Datagen implements DataGeneratorEntrypoint{
	@Override public String getEffectiveModId(){
		return ParagliderAPI.MODID;
	}

	@Override public void onInitializeDataGenerator(FabricDataGenerator dataGen){
		FabricDataGenerator.Pack pack = dataGen.createPack();
		pack.addProvider(AdvancementGen::new);
		pack.addProvider(BargainTypeGen::new);
		pack.addProvider(BiomeTagGen::new);
		pack.addProvider(ItemTagGen::new);
		pack.addProvider(LootTableProvider::new);
		pack.addProvider(RecipeGen::new);
	}
}
