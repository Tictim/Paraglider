package tictim.paraglider.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.StaminaEfficiencyAttribute;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.block.GoddessStatueBlock;
import tictim.paraglider.contents.block.HornedStatueBlock;
import tictim.paraglider.contents.item.*;
import tictim.paraglider.contents.loot.*;
import tictim.paraglider.contents.mobeffect.StaminaEfficiencyMobEffect;
import tictim.paraglider.contents.recipe.CosmeticRecipe;
import tictim.paraglider.contents.recipe.SimpleBargain;
import tictim.paraglider.contents.recipe.SimpleBargainSerializer;
import tictim.paraglider.contents.recipe.WaterBottleIngredientType;
import tictim.paraglider.contents.recipe.preview.IngredientPreview;
import tictim.paraglider.contents.recipe.preview.ItemPreview;
import tictim.paraglider.contents.recipe.preview.VesselPreview;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;
import tictim.paraglider.impl.AttachmentProvider;
import tictim.paraglider.impl.MovementState;
import tictim.paraglider.impl.SimpleVesselContainer;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.stamina.BotWStaminaData;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.contents.CommonContents.*;

public class Contents {
	public static @NotNull Contents get() {
		return ParagliderMod.instance().getContents();
	}

	public final DeferredRegister<Block> blocks = DeferredRegister.create(Registries.BLOCK, MODID);
	public final DeferredRegister<Item> items = DeferredRegister.create(Registries.ITEM, MODID);
	public final DeferredRegister<Attribute> attributes = DeferredRegister.create(Registries.ATTRIBUTE, MODID);
	public final DeferredRegister<DataComponentType<?>> dataComponents = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
	public final DeferredRegister<RecipeSerializer<?>> recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
	public final DeferredRegister<RecipeType<?>> recipeTypes = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
	public final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> loots = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
	public final DeferredRegister<LootItemConditionType> lootConditions = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
	public final DeferredRegister<MobEffect> mobEffects = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
	public final DeferredRegister<StructureType<?>> structureTypes = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
	public final DeferredRegister<StructurePieceType> pieces = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
	public final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	public final DeferredRegister<AttachmentType<?>> attachmentTypes = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
	public final DeferredRegister<MapCodec<? extends ICondition>> conditionSerializers = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, MODID);
	public final DeferredRegister<IngredientType<?>> ingredientTypes = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);
	public final DeferredRegister<BargainPreview.Type<?>> bargainPreviewTypes = DeferredRegister.create(BargainPreview.TYPE_REGISTRY_KEY, MODID);

	public final DeferredHolder<Block, Block> goddessStatue = blocks.register("goddess_statue",
			id -> new GoddessStatueBlock(statueBlock()));
	public final DeferredHolder<Block, Block> kakarikoGoddessStatue = blocks.register("kakariko_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock()));
	public final DeferredHolder<Block, Block> goronGoddessStatue = blocks.register("goron_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock().lightLevel(value -> 15)));
	public final DeferredHolder<Block, Block> ritoGoddessStatue = blocks.register("rito_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock()));
	public final DeferredHolder<Block, Block> hornedStatue = blocks.register("horned_statue",
			id -> new HornedStatueBlock(statueBlock()));

	public final DeferredHolder<Attribute, Attribute> maxStamina = attributes.register("max_stamina",
			() -> new RangedAttribute("attribute.paraglider.max_stamina", 0, 0, Double.MAX_VALUE).setSyncable(true));
	public final DeferredHolder<Attribute, Attribute> staminaEfficiency = attributes.register("stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.stamina_efficiency"));
	public final DeferredHolder<Attribute, Attribute> staminaRecovery = attributes.register("stamina_recovery",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.stamina_recovery"));
	public final DeferredHolder<Attribute, Attribute> paraglidingStaminaEfficiency = attributes.register("paragliding_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.paragliding_stamina_efficiency"));
	public final DeferredHolder<Attribute, Attribute> runningStaminaEfficiency = attributes.register("running_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.running_stamina_efficiency"));
	public final DeferredHolder<Attribute, Attribute> swimmingStaminaEfficiency = attributes.register("swimming_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.swimming_stamina_efficiency"));

	public final DeferredHolder<DataComponentType<?>, DataComponentType<ParaglidingFlag>> paraglidingFlagComponent = dataComponents.register("paragliding",
			() -> DataComponentType.<ParaglidingFlag>builder()
					.persistent(Codec.unit(ParaglidingFlag.INSTANCE))
					.networkSynchronized(StreamCodec.unit(ParaglidingFlag.INSTANCE))
					.build());

	public final DeferredHolder<Item, Item> paraglider = items.register("paraglider",
			id -> new ParagliderItem(p().stacksTo(1)));
	public final DeferredHolder<Item, Item> dekuLeaf = items.register("deku_leaf",
			id -> new ParagliderItem(p().stacksTo(1)));
	public final DeferredHolder<Item, Item> heartContainer = items.register("heart_container",
			id -> new HeartContainerItem(p().rarity(Rarity.RARE)));
	public final DeferredHolder<Item, Item> staminaVessel = items.register("stamina_vessel",
			id -> new StaminaVesselItem(p().rarity(Rarity.RARE)));
	public final DeferredHolder<Item, Item> spiritOrb = items.register("spirit_orb",
			id -> new SpiritOrbItem(p().rarity(Rarity.UNCOMMON)));
	public final DeferredHolder<Item, Item> antiVessel = items.register("anti_vessel",
			id -> new AntiVesselItem(p().rarity(Rarity.EPIC)));
	public final DeferredHolder<Item, Item> essence = items.register("essence",
			id -> new EssenceItem(p().rarity(Rarity.RARE)));
	public final DeferredHolder<Item, Item> energizingElixir1 = items.register("energizing_elixir_1",
			id -> new BaseElixirItem.Energizing(staminaPotion(), 1000));
	public final DeferredHolder<Item, Item> energizingElixir2 = items.register("energizing_elixir_2",
			id -> new BaseElixirItem.Energizing(staminaPotion(), 2000, 0, 60 * 20));
	public final DeferredHolder<Item, Item> energizingElixir3 = items.register("energizing_elixir_3",
			id -> new BaseElixirItem.Energizing(staminaPotion(), 3000, 1, 120 * 20));
	public final DeferredHolder<Item, Item> enduringElixir1 = items.register("enduring_elixir_1",
			id -> new BaseElixirItem.Enduring(staminaPotion(), 500));
	public final DeferredHolder<Item, Item> enduringElixir2 = items.register("enduring_elixir_2",
			id -> new BaseElixirItem.Enduring(staminaPotion(), 1000));
	public final DeferredHolder<Item, Item> enduringElixir3 = items.register("enduring_elixir_3",
			id -> new BaseElixirItem.Enduring(staminaPotion(), 2000));
	public final DeferredHolder<Item, Item> energizingMixture = items.register("energizing_mixture",
			id -> new Item(p().stacksTo(1)));
	public final DeferredHolder<Item, Item> enduringMixture = items.register("enduring_mixture",
			id -> new Item(p().stacksTo(1)));

	public final DeferredHolder<Item, BlockItem> goddessStatueItem = items.register("goddess_statue",
			id -> new BlockItem(goddessStatue(), p().rarity(Rarity.RARE)));
	public final DeferredHolder<Item, BlockItem> kakarikoGoddessStatueItem = items.register("kakariko_goddess_statue",
			id -> new TooltipBlockItem(kakarikoGoddessStatue(), p().rarity(Rarity.RARE), kakarikoStatueTooltip()));
	public final DeferredHolder<Item, BlockItem> goronGoddessStatueItem = items.register("goron_goddess_statue",
			id -> new TooltipBlockItem(goronGoddessStatue(), p().rarity(Rarity.RARE), goronStatueTooltip()));
	public final DeferredHolder<Item, BlockItem> ritoGoddessStatueItem = items.register("rito_goddess_statue",
			id -> new TooltipBlockItem(ritoGoddessStatue(), p().rarity(Rarity.RARE), ritoStatueTooltip()));
	public final DeferredHolder<Item, BlockItem> hornedStatueItem = items.register("horned_statue",
			id -> new TooltipBlockItem(hornedStatue(), p().rarity(Rarity.EPIC),
					Component.translatable("tooltip.paraglider.horned_statue.0")
							.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));

	public final DeferredHolder<RecipeSerializer<?>, CosmeticRecipe.Serializer> cosmeticRecipe = recipeSerializers.register("cosmetic", CosmeticRecipe.Serializer::new);
	public final DeferredHolder<RecipeSerializer<?>, SimpleBargainSerializer> bargainRecipe = recipeSerializers.register("statue_bargain", SimpleBargainSerializer::new);

	public final DeferredHolder<RecipeType<?>, RecipeType<Bargain>> bargainRecipeType = recipeTypes.register("bargain",
			() -> RecipeType.simple(ParagliderAPI.id("bargain")));

	public final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ParagliderLoot>> paragliderLoot = loots.register("paraglider", () -> ParagliderLoot.CODEC);
	public final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SpiritOrbLoot>> spiritOrbLoot = loots.register("spirit_orb", () -> SpiritOrbLoot.CODEC);
	public final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<VesselLoot>> vesselLoot = loots.register("vessel", () -> VesselLoot.CODEC);
	public final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SpawnerSpiritOrbLoot>> spawnerSpiritOrbLoot = loots.register("spawner_spirit_orb", () -> SpawnerSpiritOrbLoot.CODEC);

	public final DeferredHolder<LootItemConditionType, LootItemConditionType> witherDropsVesselConfigCondition = lootConditions.register("config_wither_drops_vessel",
			() -> new LootItemConditionType(MapCodec.unit(LootConditions.WITHER_DROPS_VESSEL)));
	public final DeferredHolder<LootItemConditionType, LootItemConditionType> spiritOrbLootsConfigCondition = lootConditions.register("config_spirit_orb_loots",
			() -> new LootItemConditionType(MapCodec.unit(LootConditions.SPIRIT_ORB_LOOTS)));

	public final DeferredHolder<MobEffect, MobEffect> staminaEfficiencyEffect = mobEffects.register("stamina_efficiency", StaminaEfficiencyMobEffect::new);

	public final DeferredHolder<StructureType<?>, StructureType<TarreyTownGoddessStatue>> tarreyTownGoddessStatue = structureType("tarrey_town_goddess_statue", TarreyTownGoddessStatue.CODEC);
	public final DeferredHolder<StructureType<?>, StructureType<NetherHornedStatue>> netherHornedStatue = structureType("nether_horned_statue", NetherHornedStatue.CODEC);
	public final DeferredHolder<StructureType<?>, StructureType<UndergroundHornedStatue>> undergroundHornedStatue = structureType("underground_horned_statue", UndergroundHornedStatue.CODEC);

	private <T extends Structure> DeferredHolder<StructureType<?>, StructureType<T>> structureType(String id, MapCodec<T> codec) {
		return structureTypes.register(id, () -> () -> codec);
	}

	public final DeferredHolder<StructurePieceType, StructurePieceType> tarreyTownGoddessStatuePiece = pieces.register("tarrey_town_goddess_statue", TarreyTownGoddessStatue::pieceType);
	public final DeferredHolder<StructurePieceType, StructurePieceType> netherHornedStatuePiece = pieces.register("nether_horned_statue", NetherHornedStatue::pieceType);
	public final DeferredHolder<StructurePieceType, StructurePieceType> undergroundHornedStatuePiece = pieces.register("underground_horned_statue", UndergroundHornedStatue::pieceType);

	public final DeferredHolder<CreativeModeTab, CreativeModeTab> tab = creativeTabs.register(MODID, () -> CreativeModeTab.builder()
			.icon(() -> new ItemStack(paraglider.get()))
			.title(Component.translatable("itemGroup." + MODID))
			.displayItems((features, out) -> {
				out.accept(paraglider());
				out.accept(dekuLeaf());
				out.accept(heartContainer());
				out.accept(staminaVessel());
				out.accept(spiritOrb());
				out.accept(antiVessel());
				out.accept(essence());
				out.accept(energizingElixir1());
				out.accept(energizingElixir2());
				out.accept(energizingElixir3());
				out.accept(enduringElixir1());
				out.accept(enduringElixir2());
				out.accept(enduringElixir3());
				out.accept(energizingMixture());
				out.accept(enduringMixture());

				out.accept(goddessStatue());
				out.accept(kakarikoGoddessStatue());
				out.accept(goronGoddessStatue());
				out.accept(ritoGoddessStatue());
				out.accept(hornedStatue());
			}).build());

	public final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerMovement>> playerMovement = attachmentTypes.register("player_movement",
			() -> AttachmentType.builder(h -> {
				if (h instanceof Player p) return AttachmentProvider.createPlayerMovement(p);
				else throw new IllegalArgumentException(
						"Cannot create player movement data attachment for non-player holder");
			}).build());

	public final DeferredHolder<AttachmentType<?>, AttachmentType<BotWStaminaData>> botwStaminaData = attachmentTypes.register("botw_stamina",
			() -> AttachmentType.builder(h -> new BotWStaminaData())
					.serialize(BotWStaminaData.CODEC)
					.build());

	public final DeferredHolder<AttachmentType<?>, AttachmentType<SimpleVesselContainer>> vesselContainer = attachmentTypes.register("vessel_container",
			() -> AttachmentType.builder(h -> new SimpleVesselContainer())
					.serialize(SimpleVesselContainer.CODEC)
					.copyOnDeath()
					.build());

	public final DeferredHolder<AttachmentType<?>, AttachmentType<MovementState>> movementState = attachmentTypes.register("movement_state",
			() -> AttachmentType.builder(h -> new MovementState())
					.serialize(MovementState.CODEC)
					.build());

	public final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> movementInitialized = attachmentTypes.register("movement_init",
			() -> AttachmentType.builder(h -> false)
					.serialize(Codec.BOOL)
					.build());

	public final DeferredHolder<IngredientType<?>, IngredientType<?>> waterBottle = ingredientTypes.register("water_bottle", WaterBottleIngredientType.INSTANCE::getType);

	public final DeferredHolder<BargainPreview.Type<?>, BargainPreview.Type<ItemPreview>> itemPreviewType = bargainPreviewTypes.register("item",
			() -> ItemPreview.TYPE);

	public final DeferredHolder<BargainPreview.Type<?>, BargainPreview.Type<IngredientPreview>> ingredientPreviewType = bargainPreviewTypes.register("ingredient",
			() -> IngredientPreview.TYPE);

	public final DeferredHolder<BargainPreview.Type<?>, BargainPreview.Type<VesselPreview>> vesselPreviewType = bargainPreviewTypes.register("vessel",
			() -> VesselPreview.TYPE);

	public Contents(IEventBus eventBus) {
		this.blocks.register(eventBus);
		this.items.register(eventBus);
		this.attributes.register(eventBus);
		this.dataComponents.register(eventBus);
		this.loots.register(eventBus);
		this.lootConditions.register(eventBus);
		this.mobEffects.register(eventBus);
		this.recipeSerializers.register(eventBus);
		this.recipeTypes.register(eventBus);
		this.structureTypes.register(eventBus);
		this.pieces.register(eventBus);
		this.creativeTabs.register(eventBus);
		this.attachmentTypes.register(eventBus);
		this.conditionSerializers.register(eventBus);
		this.ingredientTypes.register(eventBus);
		this.bargainPreviewTypes.register(eventBus);

		ParagliderConfigCondition.register(this.conditionSerializers);
	}

	public @NotNull Item paraglider() {
		return paraglider.get();
	}
	public @NotNull Item dekuLeaf() {
		return dekuLeaf.get();
	}
	public @NotNull Item heartContainer() {
		return heartContainer.get();
	}
	public @NotNull Item staminaVessel() {
		return staminaVessel.get();
	}
	public @NotNull Item spiritOrb() {
		return spiritOrb.get();
	}
	public @NotNull Item antiVessel() {
		return antiVessel.get();
	}
	public @NotNull Item essence() {
		return essence.get();
	}
	public @NotNull Item energizingElixir1() {
		return energizingElixir1.get();
	}
	public @NotNull Item energizingElixir2() {
		return energizingElixir2.get();
	}
	public @NotNull Item energizingElixir3() {
		return energizingElixir3.get();
	}
	public @NotNull Item enduringElixir1() {
		return enduringElixir1.get();
	}
	public @NotNull Item enduringElixir2() {
		return enduringElixir2.get();
	}
	public @NotNull Item enduringElixir3() {
		return enduringElixir3.get();
	}
	public @NotNull Item energizingMixture() {
		return energizingMixture.get();
	}
	public @NotNull Item enduringMixture() {
		return enduringMixture.get();
	}
	public @NotNull Block goddessStatue() {
		return goddessStatue.get();
	}
	public @NotNull Block kakarikoGoddessStatue() {
		return kakarikoGoddessStatue.get();
	}
	public @NotNull Block goronGoddessStatue() {
		return goronGoddessStatue.get();
	}
	public @NotNull Block ritoGoddessStatue() {
		return ritoGoddessStatue.get();
	}
	public @NotNull Block hornedStatue() {
		return hornedStatue.get();
	}
	public @NotNull BlockItem goddessStatueItem() {
		return goddessStatueItem.get();
	}
	public @NotNull BlockItem kakarikoGoddessStatueItem() {
		return kakarikoGoddessStatueItem.get();
	}
	public @NotNull BlockItem goronGoddessStatueItem() {
		return goronGoddessStatueItem.get();
	}
	public @NotNull BlockItem ritoGoddessStatueItem() {
		return ritoGoddessStatueItem.get();
	}
	public @NotNull BlockItem hornedStatueItem() {
		return hornedStatueItem.get();
	}
	public @NotNull Holder<Attribute> maxStamina() {
		return maxStamina;
	}
	public @NotNull Holder<Attribute> staminaEfficiency() {
		return staminaEfficiency;
	}
	public @NotNull Holder<Attribute> staminaRecovery() {
		return staminaRecovery;
	}
	public @NotNull Holder<Attribute> paraglidingStaminaEfficiency() {
		return paraglidingStaminaEfficiency;
	}
	public @NotNull Holder<Attribute> runningStaminaEfficiency() {
		return runningStaminaEfficiency;
	}
	public @NotNull Holder<Attribute> swimmingStaminaEfficiency() {
		return swimmingStaminaEfficiency;
	}
	public @NotNull DataComponentType<ParaglidingFlag> paraglidingFlagComponent() {
		return paraglidingFlagComponent.get();
	}
	public @NotNull CosmeticRecipe.Serializer cosmeticRecipeSerializer() {
		return cosmeticRecipe.get();
	}
	public @NotNull RecipeSerializer<SimpleBargain> bargainRecipeSerializer() {
		return bargainRecipe.get();
	}
	public @NotNull RecipeType<Bargain> bargainRecipeType() {
		return bargainRecipeType.get();
	}
	public @NotNull StructureType<TarreyTownGoddessStatue> tarreyTownGoddessStatue() {
		return tarreyTownGoddessStatue.get();
	}
	public @NotNull StructureType<NetherHornedStatue> netherHornedStatue() {
		return netherHornedStatue.get();
	}
	public @NotNull StructureType<UndergroundHornedStatue> undergroundHornedStatue() {
		return undergroundHornedStatue.get();
	}
	public @NotNull StructurePieceType tarreyTownGoddessStatuePiece() {
		return tarreyTownGoddessStatuePiece.get();
	}
	public @NotNull StructurePieceType netherHornedStatuePiece() {
		return netherHornedStatuePiece.get();
	}
	public @NotNull StructurePieceType undergroundHornedStatuePiece() {
		return undergroundHornedStatuePiece.get();
	}
	public @NotNull AttachmentType<PlayerMovement> playerMovement() {
		return playerMovement.get();
	}
	public @NotNull AttachmentType<BotWStaminaData> botwStaminaData() {
		return botwStaminaData.get();
	}
	public @NotNull AttachmentType<SimpleVesselContainer> vesselContainer() {
		return vesselContainer.get();
	}
	public @NotNull AttachmentType<MovementState> movementState() {
		return movementState.get();
	}
	public @NotNull AttachmentType<Boolean> movementInitialized() {
		return movementInitialized.get();
	}
}
