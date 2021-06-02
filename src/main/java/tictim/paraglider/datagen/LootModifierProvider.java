package tictim.paraglider.datagen;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.EntityHasProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.loot.ItemModifier;
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

		addSpiritOrbItemModifier("wither", new ItemModifier(
				new ILootCondition[]{
						EntityHasProperty.builder(LootContext.EntityTarget.THIS, EntityPredicate.Builder.create().type(EntityType.WITHER)).build(),
						KilledByPlayer.builder().build()
				}, Contents.SPIRIT_ORB.get(), 1
		));
		addSpiritOrbItemModifier("spawner", new ItemModifier(
				new ILootCondition[]{
						BlockStateProperty.builder(Blocks.SPAWNER).build()
				}, Contents.SPIRIT_ORB.get(), 1
		));
		addChestSpiritOrbItemModifier("underwater_ruin_big", .5f);
		addChestSpiritOrbItemModifier("underwater_ruin_small", .5f);
		addChestSpiritOrbItemModifier("jungle_temple");
		addChestSpiritOrbItemModifier("desert_pyramid", .5f);
		addChestSpiritOrbItemModifier("bastion_other");
		addChestSpiritOrbItemModifier("bastion_bridge");
		addChestSpiritOrbItemModifier("bastion_treasure");
		addChestSpiritOrbItemModifier("bastion_hoglin_stable");
		addChestSpiritOrbItemModifier("stronghold_corridor", .5f);
		addChestSpiritOrbItemModifier("stronghold_crossing", .5f);
		addChestSpiritOrbItemModifier("stronghold_library");
		addChestSpiritOrbItemModifier("nether_bridge", .5f);
		addChestSpiritOrbItemModifier("buried_treasure");
	}

	private void addChestSpiritOrbItemModifier(String chestLootTableName, float chance){
		addSpiritOrbItemModifier(chestLootTableName, new ItemModifier(
				new ILootCondition[]{
						LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build(),
						RandomChance.builder(chance).build()
				}, Contents.SPIRIT_ORB.get(), 1
		));
	}
	private void addChestSpiritOrbItemModifier(String chestLootTableName){
		addSpiritOrbItemModifier(chestLootTableName, new ItemModifier(
				new ILootCondition[]{
						LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build()
				}, Contents.SPIRIT_ORB.get(), 1
		));
	}

	private void addSpiritOrbItemModifier(String modifier, ItemModifier instance){
		add("spirit_orbs/"+modifier, Contents.ITEM_MODIFIER.get(), instance);
	}
}
