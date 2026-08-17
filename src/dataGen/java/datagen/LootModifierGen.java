package datagen;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.loot.*;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class LootModifierGen extends GlobalLootModifierProvider {
	public LootModifierGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, ParagliderAPI.MODID);
	}

	@Override protected void start() {
		var entities = registries.lookupOrThrow(Registries.ENTITY_TYPE);

		add("totw_reworked/chest", new ParagliderLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(Identifier.fromNamespaceAndPath("totw_reworked", "tower_chest")).build(),
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				false
		));
		add("totw_reworked/ocean_chest", new ParagliderLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(Identifier.fromNamespaceAndPath("totw_reworked", "ocean_tower_chest")).build(),
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				true
		));

		add("wither", new VesselLoot(
				new LootItemCondition[]{
						LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity().of(entities, EntityTypes.WITHER)).build(),
						LootItemKilledByPlayerCondition.killedByPlayer().build(),
						ParagliderLootConditions.WITHER_DROPS_VESSEL
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				1
		));

		add("elder_guardian", new SpiritOrbLoot(
				new LootItemCondition[]{
						LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity().of(entities, EntityTypes.ELDER_GUARDIAN)).build(),
						LootItemKilledByPlayerCondition.killedByPlayer().build(),
						ParagliderLootConditions.ELDER_GUARDIAN_DROPS_SPIRIT_ORB
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				1
		));

		addSpiritOrbItemModifier("spawner", new ConfigurableSpiritOrbLoot(
				new LootItemCondition[]{
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SPAWNER).build()
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				ConfigurableSpiritOrbLoot.Type.SPAWNER
		));

		addSpiritOrbItemModifier("trial_reward", new ConfigurableSpiritOrbLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(BuiltInLootTables.TRIAL_CHAMBERS_REWARD.identifier()).build()
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				ConfigurableSpiritOrbLoot.Type.TRIAL
		));

		addSpiritOrbItemModifier("ominous_trial_reward", new ConfigurableSpiritOrbLoot(
				new LootItemCondition[]{
						LootTableIdCondition.builder(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS.identifier()).build()
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				ConfigurableSpiritOrbLoot.Type.OMINOUS_TRIAL
		));

		addChestSpiritOrbItemModifier("underwater_ruin_big", .25f);
		addChestSpiritOrbItemModifier("underwater_ruin_small", .25f);
		addChestSpiritOrbItemModifier("jungle_temple", .66f);
		addChestSpiritOrbItemModifier("desert_pyramid", .25f);
		addChestSpiritOrbItemModifier("bastion_other");
		addChestSpiritOrbItemModifier("bastion_bridge");
		addChestSpiritOrbItemModifier("bastion_treasure");
		addChestSpiritOrbItemModifier("bastion_hoglin_stable");
		addChestSpiritOrbItemModifier("stronghold_corridor", .5f);
		addChestSpiritOrbItemModifier("stronghold_crossing", .5f);
		addChestSpiritOrbItemModifier("stronghold_library");
		addChestSpiritOrbItemModifier("nether_bridge", .5f);
		addChestSpiritOrbItemModifier("buried_treasure");
		addChestSpiritOrbItemModifier("woodland_mansion");
		addChestSpiritOrbItemModifier("ancient_city");
	}

	private void addChestSpiritOrbItemModifier(String chestLootTableName, float chance) {
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				new LootItemCondition[]{
						ParagliderLootConditions.SPIRIT_ORB_LOOTS,
						LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/" + chestLootTableName)).build(),
						LootItemRandomChanceCondition.randomChance(chance).build()
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				1
		));
	}
	private void addChestSpiritOrbItemModifier(String chestLootTableName) {
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				new LootItemCondition[]{
						ParagliderLootConditions.SPIRIT_ORB_LOOTS,
						LootTableIdCondition.builder(Identifier.withDefaultNamespace("chests/" + chestLootTableName)).build()
				},
				IGlobalLootModifier.DEFAULT_PRIORITY,
				1
		));
	}

	private void addSpiritOrbItemModifier(String modifier, IGlobalLootModifier instance) {
		add("spirit_orbs/" + modifier, instance);
	}
}
