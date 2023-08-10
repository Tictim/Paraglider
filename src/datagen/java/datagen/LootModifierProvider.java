package datagen;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.contents.loot.ParagliderLoot;
import tictim.paraglider.contents.loot.SpiritOrbLoot;
import tictim.paraglider.contents.loot.VesselLoot;

public class LootModifierProvider extends GlobalLootModifierProvider{
	public LootModifierProvider(PackOutput output){
		super(output, ParagliderMod.MODID);
	}

	@Override protected void start(){
		add("totw_reworked/chest", new ParagliderLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(
								new ResourceLocation("totw_reworked", "tower_chest")
						).build()
				}
		));
		add("totw_reworked/ocean_chest", new ParagliderLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(
								new ResourceLocation("totw_reworked", "ocean_tower_chest")
						).build()
				}, true
		));

		add("wither", new VesselLoot(
				new LootItemCondition[]{
						LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(EntityType.WITHER)).build(),
						LootItemKilledByPlayerCondition.killedByPlayer().build()
				}, 1
		));
		addSpiritOrbItemModifier("spawner", new SpiritOrbLoot(
				new LootItemCondition[]{
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SPAWNER).build()
				}, 2
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
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build(),
						LootItemRandomChanceCondition.randomChance(chance).build()
				}, 1
		));
	}
	private void addChestSpiritOrbItemModifier(String chestLootTableName){
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build()
				}, 1
		));
	}

	private void addSpiritOrbItemModifier(String modifier, SpiritOrbLoot instance){
		add("spirit_orbs/"+modifier, instance);
	}
}
