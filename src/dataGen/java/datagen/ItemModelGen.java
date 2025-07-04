package datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import tictim.paraglider.contents.Contents;

import static tictim.paraglider.api.ParagliderAPI.MODID;

public class ItemModelGen extends ItemModelProvider {
	public ItemModelGen(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, MODID, existingFileHelper);
	}

	@Override protected void registerModels() {
		Contents c = Contents.get();

		// paraglider/deku leaf already have models

		basicItem(c.heartContainer());
		basicItem(c.staminaVessel());
		basicItem(c.spiritOrb());
		basicItem(c.antiVessel());
		basicItem(c.essence());

		basicItem(c.energizingElixir1());
		basicItem(c.energizingElixir2());
		basicItem(c.energizingElixir3());
		basicItem(c.enduringElixir1());
		basicItem(c.enduringElixir2());
		basicItem(c.enduringElixir3());
		basicItem(c.energizingMixture());
		basicItem(c.enduringMixture());
	}
}
