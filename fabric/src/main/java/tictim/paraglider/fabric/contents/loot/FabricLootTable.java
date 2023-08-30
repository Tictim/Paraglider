package tictim.paraglider.fabric.contents.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import tictim.paraglider.contents.Contents;

public final class FabricLootTable{
	private FabricLootTable(){}

	private static final ResourceLocation WITHER_DROPS = EntityType.WITHER.getDefaultLootTable();
	private static final ResourceLocation SPAWNER_DROPS = Blocks.SPAWNER.getLootTable();
	private static final ResourceLocation TOTW_REWORKED_CHEST = new ResourceLocation("totw_reworked", "tower_chest");
	private static final ResourceLocation TOTW_REWORKED_OCEAN_CHEST = new ResourceLocation("totw_reworked", "ocean_tower_chest");

	public static void modifyLootTables(ResourceManager resourceManager,
	                                    LootDataManager lootManager,
	                                    ResourceLocation id,
	                                    LootTable.Builder lootTable,
	                                    LootTableSource source){
		if(!source.isBuiltin()) return;

		if(WITHER_DROPS.equals(id)){
			lootTable.withPool(LootPool.lootPool()
					.add(VesselLootEntry.builder()
							.when(LootConditions.WITHER_DROPS_VESSEL)));
		}else if(SPAWNER_DROPS.equals(id)){
			lootTable.withPool(LootPool.lootPool()
					.add(LootItem.lootTableItem(Contents.get().spiritOrb())
							.when(LootConditions.FEATURES_SPIRIT_ORBS)
							.apply(LootFunctions.SPAWNER_SPIRIT_ORB_COUNT)));
		}else if(BuiltInLootTables.UNDERWATER_RUIN_BIG.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.UNDERWATER_RUIN_SMALL.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.JUNGLE_TEMPLE.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.DESERT_PYRAMID.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.BASTION_OTHER.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.BASTION_BRIDGE.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.BASTION_TREASURE.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.BASTION_HOGLIN_STABLE.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.STRONGHOLD_CORRIDOR.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.STRONGHOLD_CROSSING.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.STRONGHOLD_LIBRARY.equals(id)) addChestSpiritOrb(lootTable);
		else if(BuiltInLootTables.NETHER_BRIDGE.equals(id)) addChestSpiritOrb(lootTable, .5f);
		else if(BuiltInLootTables.BURIED_TREASURE.equals(id)) addChestSpiritOrb(lootTable);
		else if(TOTW_REWORKED_CHEST.equals(id)){
			lootTable.withPool(LootPool.lootPool().add(ParagliderLootEntry.builder(false)));
		}else if(TOTW_REWORKED_OCEAN_CHEST.equals(id)){
			lootTable.withPool(LootPool.lootPool().add(ParagliderLootEntry.builder(true)));
		}
	}

	private static void addChestSpiritOrb(LootTable.Builder builder){
		builder.withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(Contents.get().spiritOrb())
						.when(LootConditions.SPIRIT_ORB_LOOTS)));
	}

	private static void addChestSpiritOrb(LootTable.Builder builder, float chance){
		builder.withPool(LootPool.lootPool()
				.add(LootItem.lootTableItem(Contents.get().spiritOrb())
						.when(LootConditions.SPIRIT_ORB_LOOTS)
						.when(LootItemRandomChanceCondition.randomChance(chance))));
	}
}
