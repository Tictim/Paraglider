package tictim.paraglider.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.loot.ParagliderModifier;

public class LootModifierProvider extends GlobalLootModifierProvider{
	public LootModifierProvider(DataGenerator gen, String modid){
		super(gen, modid);
	}

	@Override protected void start(){
		add("towers_of_the_wild/chest", Contents.PARAGLIDER_MODIFIER.get(), new ParagliderModifier(
				new ILootCondition[]{
						LootTableIdCondition.builder(
								new ResourceLocation("towers_of_the_wild", "chests/tower/regular/tower_chest")
						).build()
				}
		));
		add("towers_of_the_wild/ocean_chest", Contents.PARAGLIDER_MODIFIER.get(), new ParagliderModifier(
				new ILootCondition[]{
						LootTableIdCondition.builder(
								new ResourceLocation("towers_of_the_wild", "chests/tower/ocean/ocean_tower_chest")
						).build()
				}, true
		));
	}
}
