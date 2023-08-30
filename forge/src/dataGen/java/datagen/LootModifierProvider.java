package datagen;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.forge.contents.loot.LootConditions;
import tictim.paraglider.forge.contents.loot.ParagliderLoot;
import tictim.paraglider.forge.contents.loot.SpawnerSpiritOrbLoot;
import tictim.paraglider.forge.contents.loot.SpiritOrbLoot;
import tictim.paraglider.forge.contents.loot.VesselLoot;

public class LootModifierProvider extends GlobalLootModifierProvider{
	public LootModifierProvider(@NotNull PackOutput output){
		super(output, ParagliderAPI.MODID);
	}

	@Override protected void start(){
		add("totw_reworked/chest", new ParagliderLoot(
				false,
				LootTableIdCondition.builder(new ResourceLocation("totw_reworked", "tower_chest")).build()
		));
		add("totw_reworked/ocean_chest", new ParagliderLoot(
				true,
				LootTableIdCondition.builder(new ResourceLocation("totw_reworked", "ocean_tower_chest")).build()
		));

		add("wither", new VesselLoot(
				1,
				LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(EntityType.WITHER)).build(),
				LootItemKilledByPlayerCondition.killedByPlayer().build(),
				LootConditions.WITHER_DROPS_VESSEL
		));

		addSpiritOrbItemModifier("spawner", new SpawnerSpiritOrbLoot(
				LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SPAWNER).build()
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
				1,
				LootConditions.SPIRIT_ORB_LOOTS,
				LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build(),
				LootItemRandomChanceCondition.randomChance(chance).build()
		));
	}
	private void addChestSpiritOrbItemModifier(String chestLootTableName){
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				1,
				LootConditions.SPIRIT_ORB_LOOTS,
				LootTableIdCondition.builder(new ResourceLocation("chests/"+chestLootTableName)).build()
		));
	}

	private void addSpiritOrbItemModifier(String modifier, IGlobalLootModifier instance){
		add("spirit_orbs/"+modifier, instance);
	}
}
