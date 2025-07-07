package datagen;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.contents.loot.*;

import java.util.concurrent.CompletableFuture;

public class LootModifierGen extends GlobalLootModifierProvider {
	public LootModifierGen(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, ParagliderAPI.MODID);
	}

	@Override protected void start() {
		var entities = registries.lookupOrThrow(Registries.ENTITY_TYPE);

		add("totw_reworked/chest", new ParagliderLoot(
				false,
				LootTableIdCondition.builder(ResourceLocation.fromNamespaceAndPath("totw_reworked", "tower_chest")).build()
		));
		add("totw_reworked/ocean_chest", new ParagliderLoot(
				true,
				LootTableIdCondition.builder(ResourceLocation.fromNamespaceAndPath("totw_reworked", "ocean_tower_chest")).build()
		));

		add("wither", new VesselLoot(
				1,
				LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(entities, EntityType.WITHER)).build(),
				LootItemKilledByPlayerCondition.killedByPlayer().build(),
				ParagliderLootConditions.WITHER_DROPS_VESSEL
		));

		add("elder_guardian", new SpiritOrbLoot(
				1,
				LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
						EntityPredicate.Builder.entity().of(EntityType.ELDER_GUARDIAN)).build(),
				LootItemKilledByPlayerCondition.killedByPlayer().build(),
				ParagliderLootConditions.ELDER_GUARDIAN_DROPS_SPIRIT_ORB
		));

		addSpiritOrbItemModifier("spawner", new ConfigurableSpiritOrbLoot(
				ConfigurableSpiritOrbLoot.Type.SPAWNER,
				LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SPAWNER).build()
		));

		addSpiritOrbItemModifier("trial_reward", new ConfigurableSpiritOrbLoot(
				ConfigurableSpiritOrbLoot.Type.TRIAL,
				LootTableIdCondition.builder(BuiltInLootTables.TRIAL_CHAMBERS_REWARD.location()).build()
		));

		addSpiritOrbItemModifier("ominous_trial_reward", new ConfigurableSpiritOrbLoot(
				ConfigurableSpiritOrbLoot.Type.OMINOUS_TRIAL,
				LootTableIdCondition.builder(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS.location()).build()
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
				1,
				ParagliderLootConditions.SPIRIT_ORB_LOOTS,
				LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/" + chestLootTableName)).build(),
				LootItemRandomChanceCondition.randomChance(chance).build()
		));
	}
	private void addChestSpiritOrbItemModifier(String chestLootTableName) {
		addSpiritOrbItemModifier(chestLootTableName, new SpiritOrbLoot(
				1,
				ParagliderLootConditions.SPIRIT_ORB_LOOTS,
				LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/" + chestLootTableName)).build()
		));
	}

	private void addSpiritOrbItemModifier(String modifier, IGlobalLootModifier instance) {
		add("spirit_orbs/" + modifier, instance);
	}
}
