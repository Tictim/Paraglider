package tictim.paraglider.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
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
import net.neoforged.neoforge.registries.*;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.StaminaEfficiencyAttribute;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.contents.block.GoddessStatueBlock;
import tictim.paraglider.contents.block.HornedStatueBlock;
import tictim.paraglider.contents.item.*;
import tictim.paraglider.contents.item.consumeeffect.GiveExtraStaminaConsumeEffect;
import tictim.paraglider.contents.item.consumeeffect.GiveStaminaEfficiencyConsumeEffect;
import tictim.paraglider.contents.item.consumeeffect.RestoreStaminaConsumeEffect;
import tictim.paraglider.contents.loot.*;
import tictim.paraglider.contents.mobeffect.StaminaEfficiencyMobEffect;
import tictim.paraglider.contents.recipe.CosmeticRecipe;
import tictim.paraglider.contents.recipe.SimpleBargain;
import tictim.paraglider.contents.recipe.SimpleBargainSerializer;
import tictim.paraglider.contents.recipe.WaterBottleIngredientType;
import tictim.paraglider.contents.recipe.preview.SimplePreview;
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

	public final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(MODID);
	public final DeferredRegister.Items items = DeferredRegister.createItems(MODID);
	public final DeferredRegister<@NotNull Attribute> attributes = DeferredRegister.create(Registries.ATTRIBUTE, MODID);
	public final DeferredRegister<ConsumeEffect.@NotNull Type<?>> consumeEffectTypes = DeferredRegister.create(Registries.CONSUME_EFFECT_TYPE, MODID);
	public final DeferredRegister<@NotNull DataComponentType<?>> dataComponents = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
	public final DeferredRegister<@NotNull RecipeSerializer<?>> recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
	public final DeferredRegister<@NotNull RecipeType<?>> recipeTypes = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
	public final DeferredRegister<@NotNull MapCodec<? extends IGlobalLootModifier>> loots = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
	public final DeferredRegister<@NotNull LootItemConditionType> lootConditions = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
	public final DeferredRegister<@NotNull MobEffect> mobEffects = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
	public final DeferredRegister<@NotNull StructureType<?>> structureTypes = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
	public final DeferredRegister<@NotNull StructurePieceType> pieces = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
	public final DeferredRegister<@NotNull CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	public final DeferredRegister<@NotNull AttachmentType<?>> attachmentTypes = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
	public final DeferredRegister<@NotNull MapCodec<? extends ICondition>> conditionSerializers = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, MODID);
	public final DeferredRegister<SlotDisplay.@NotNull Type<?>> slotDisplays = DeferredRegister.create(Registries.SLOT_DISPLAY, MODID);
	public final DeferredRegister<@NotNull IngredientType<?>> ingredientTypes = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);
	public final DeferredRegister<BargainPreview.@NotNull Type<?>> bargainPreviewTypes = DeferredRegister.create(BargainPreview.TYPE_REGISTRY_KEY, MODID);

	public final DeferredBlock<@NotNull Block> goddessStatue = blocks.register("goddess_statue",
			id -> new GoddessStatueBlock(statueBlock(id)));
	public final DeferredBlock<@NotNull Block> kakarikoGoddessStatue = blocks.register("kakariko_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock(id)));
	public final DeferredBlock<@NotNull Block> goronGoddessStatue = blocks.register("goron_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock(id).lightLevel(value -> 15)));
	public final DeferredBlock<@NotNull Block> ritoGoddessStatue = blocks.register("rito_goddess_statue",
			id -> new GoddessStatueBlock(statueBlock(id)));
	public final DeferredBlock<@NotNull Block> hornedStatue = blocks.register("horned_statue",
			id -> new HornedStatueBlock(statueBlock(id)));

	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> maxStamina = attributes.register("max_stamina",
			() -> new RangedAttribute("attribute.paraglider.max_stamina", 0, 0, Double.MAX_VALUE).setSyncable(true));
	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> staminaEfficiency = attributes.register("stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.stamina_efficiency"));
	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> staminaRecovery = attributes.register("stamina_recovery",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.stamina_recovery"));
	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> paraglidingStaminaEfficiency = attributes.register("paragliding_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.paragliding_stamina_efficiency"));
	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> runningStaminaEfficiency = attributes.register("running_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.running_stamina_efficiency"));
	public final DeferredHolder<@NotNull Attribute, @NotNull Attribute> swimmingStaminaEfficiency = attributes.register("swimming_stamina_efficiency",
			() -> new StaminaEfficiencyAttribute("attribute.paraglider.swimming_stamina_efficiency"));

	public final DeferredHolder<ConsumeEffect.@NotNull Type<?>, ConsumeEffect.@NotNull Type<@NotNull RestoreStaminaConsumeEffect>> restoreStaminaConsumeEffectType =
			consumeEffectTypes.register("restore_stamina", () -> RestoreStaminaConsumeEffect.TYPE);
	public final DeferredHolder<ConsumeEffect.@NotNull Type<?>, ConsumeEffect.@NotNull Type<@NotNull GiveExtraStaminaConsumeEffect>> giveExtraStaminaConsumeEffectType =
			consumeEffectTypes.register("give_extra_stamina", () -> GiveExtraStaminaConsumeEffect.TYPE);
	public final DeferredHolder<ConsumeEffect.@NotNull Type<?>, ConsumeEffect.@NotNull Type<@NotNull GiveStaminaEfficiencyConsumeEffect>> giveStaminaEfficiency =
			consumeEffectTypes.register("give_stamina_efficiency", () -> GiveStaminaEfficiencyConsumeEffect.TYPE);

	public final DeferredHolder<@NotNull DataComponentType<?>, @NotNull DataComponentType<@NotNull Unit>> paraglidingFlagComponent = dataComponents.register("paragliding",
			() -> DataComponentType.<Unit>builder()
					.persistent(Unit.CODEC)
					.networkSynchronized(Unit.STREAM_CODEC)
					.build());

	public final DeferredItem<@NotNull Item> paraglider = items.register("paraglider",
			id -> new ParagliderItem(p(id).stacksTo(1)));
	public final DeferredItem<@NotNull Item> dekuLeaf = items.register("deku_leaf",
			id -> new ParagliderItem(p(id).stacksTo(1)));
	public final DeferredItem<@NotNull Item> heartContainer = items.register("heart_container",
			id -> new HeartContainerItem(p(id).rarity(Rarity.RARE).fireResistant()));
	public final DeferredItem<@NotNull Item> staminaVessel = items.register("stamina_vessel",
			id -> new StaminaVesselItem(p(id).rarity(Rarity.RARE).fireResistant()));
	public final DeferredItem<@NotNull Item> spiritOrb = items.register("spirit_orb",
			id -> new SpiritOrbItem(p(id).rarity(Rarity.UNCOMMON).fireResistant()));
	public final DeferredItem<@NotNull Item> antiVessel = items.register("anti_vessel",
			id -> new AntiVesselItem(p(id).rarity(Rarity.EPIC).fireResistant()));
	public final DeferredItem<@NotNull Item> essence = items.register("essence",
			id -> new EssenceItem(p(id).rarity(Rarity.RARE).fireResistant()));
	public final DeferredItem<@NotNull Item> energizingElixir1 = items.register("energizing_elixir_1",
			id -> new Item(staminaPotion(id, new RestoreStaminaConsumeEffect(1000))));
	public final DeferredItem<@NotNull Item> energizingElixir2 = items.register("energizing_elixir_2",
			id -> new Item(staminaPotion(id,
					new RestoreStaminaConsumeEffect(2000),
					new GiveStaminaEfficiencyConsumeEffect(0, 60 * 20))));
	public final DeferredItem<@NotNull Item> energizingElixir3 = items.register("energizing_elixir_3",
			id -> new Item(staminaPotion(id,
					new RestoreStaminaConsumeEffect(3000),
					new GiveStaminaEfficiencyConsumeEffect(1, 120 * 20))));
	public final DeferredItem<@NotNull Item> enduringElixir1 = items.register("enduring_elixir_1",
			id -> new Item(staminaPotion(id, new GiveExtraStaminaConsumeEffect(500))));
	public final DeferredItem<@NotNull Item> enduringElixir2 = items.register("enduring_elixir_2",
			id -> new Item(staminaPotion(id, new GiveExtraStaminaConsumeEffect(1000))));
	public final DeferredItem<@NotNull Item> enduringElixir3 = items.register("enduring_elixir_3",
			id -> new Item(staminaPotion(id, new GiveExtraStaminaConsumeEffect(2000))));
	public final DeferredItem<@NotNull Item> energizingMixture = items.register("energizing_mixture",
			id -> new Item(p(id).stacksTo(1)));
	public final DeferredItem<@NotNull Item> enduringMixture = items.register("enduring_mixture",
			id -> new Item(p(id).stacksTo(1)));

	public final DeferredItem<@NotNull BlockItem> goddessStatueItem = items.register("goddess_statue",
			id -> new BlockItem(goddessStatue(), p(id).rarity(Rarity.RARE)));
	public final DeferredItem<@NotNull BlockItem> kakarikoGoddessStatueItem = items.register("kakariko_goddess_statue",
			id -> new TooltipBlockItem(kakarikoGoddessStatue(), p(id).rarity(Rarity.RARE), kakarikoStatueTooltip()));
	public final DeferredItem<@NotNull BlockItem> goronGoddessStatueItem = items.register("goron_goddess_statue",
			id -> new TooltipBlockItem(goronGoddessStatue(), p(id).rarity(Rarity.RARE), goronStatueTooltip()));
	public final DeferredItem<@NotNull BlockItem> ritoGoddessStatueItem = items.register("rito_goddess_statue",
			id -> new TooltipBlockItem(ritoGoddessStatue(), p(id).rarity(Rarity.RARE), ritoStatueTooltip()));
	public final DeferredItem<@NotNull BlockItem> hornedStatueItem = items.register("horned_statue",
			id -> new TooltipBlockItem(hornedStatue(), p(id).rarity(Rarity.EPIC),
					Component.translatable("tooltip.paraglider.horned_statue.0")
							.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));

	public final DeferredHolder<@NotNull RecipeSerializer<?>, CosmeticRecipe.@NotNull Serializer> cosmeticRecipe = recipeSerializers.register("cosmetic", CosmeticRecipe.Serializer::new);
	public final DeferredHolder<@NotNull RecipeSerializer<?>, @NotNull SimpleBargainSerializer> bargainRecipe = recipeSerializers.register("statue_bargain", SimpleBargainSerializer::new);

	public final DeferredHolder<@NotNull RecipeType<?>, @NotNull RecipeType<@NotNull Bargain>> bargainRecipeType = recipeTypes.register("bargain",
			() -> RecipeType.simple(ParagliderAPI.id("bargain")));

	public final DeferredHolder<@NotNull MapCodec<? extends IGlobalLootModifier>, @NotNull MapCodec<ParagliderLoot>> paragliderLoot = loots.register("paraglider", () -> ParagliderLoot.CODEC);
	public final DeferredHolder<@NotNull MapCodec<? extends IGlobalLootModifier>, @NotNull MapCodec<SpiritOrbLoot>> spiritOrbLoot = loots.register("spirit_orb", () -> SpiritOrbLoot.CODEC);
	public final DeferredHolder<@NotNull MapCodec<? extends IGlobalLootModifier>, @NotNull MapCodec<VesselLoot>> vesselLoot = loots.register("vessel", () -> VesselLoot.CODEC);
	public final DeferredHolder<@NotNull MapCodec<? extends IGlobalLootModifier>, @NotNull MapCodec<ConfigurableSpiritOrbLoot>> configurableSpiritOrbLoot = loots.register("configurable_spirit_orb",
			() -> ConfigurableSpiritOrbLoot.CODEC);

	public final DeferredHolder<@NotNull MobEffect, @NotNull MobEffect> staminaEfficiencyEffect = mobEffects.register("stamina_efficiency", StaminaEfficiencyMobEffect::new);

	public final DeferredHolder<@NotNull StructureType<?>, @NotNull StructureType<@NotNull TarreyTownGoddessStatue>> tarreyTownGoddessStatue = structureType("tarrey_town_goddess_statue", TarreyTownGoddessStatue.CODEC);
	public final DeferredHolder<@NotNull StructureType<?>, @NotNull StructureType<@NotNull NetherHornedStatue>> netherHornedStatue = structureType("nether_horned_statue", NetherHornedStatue.CODEC);
	public final DeferredHolder<@NotNull StructureType<?>, @NotNull StructureType<@NotNull UndergroundHornedStatue>> undergroundHornedStatue = structureType("underground_horned_statue", UndergroundHornedStatue.CODEC);

	private <T extends Structure> DeferredHolder<@NotNull StructureType<?>, @NotNull StructureType<@NotNull T>> structureType(String id, MapCodec<T> codec) {
		return structureTypes.register(id, () -> () -> codec);
	}

	public final DeferredHolder<@NotNull StructurePieceType, @NotNull StructurePieceType> tarreyTownGoddessStatuePiece = pieces.register("tarrey_town_goddess_statue", TarreyTownGoddessStatue::pieceType);
	public final DeferredHolder<@NotNull StructurePieceType, @NotNull StructurePieceType> netherHornedStatuePiece = pieces.register("nether_horned_statue", NetherHornedStatue::pieceType);
	public final DeferredHolder<@NotNull StructurePieceType, @NotNull StructurePieceType> undergroundHornedStatuePiece = pieces.register("underground_horned_statue", UndergroundHornedStatue::pieceType);

	public final DeferredHolder<@NotNull CreativeModeTab, @NotNull CreativeModeTab> tab = creativeTabs.register(MODID, () -> CreativeModeTab.builder()
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

	public final DeferredHolder<@NotNull AttachmentType<?>, @NotNull AttachmentType<@NotNull PlayerMovement>> playerMovement = attachmentTypes.register("player_movement",
			() -> AttachmentType.builder(h -> {
				if (h instanceof Player p) return AttachmentProvider.createPlayerMovement(p);
				else throw new IllegalArgumentException(
						"Cannot create player movement data attachment for non-player holder");
			}).build());

	public final DeferredHolder<@NotNull AttachmentType<?>, @NotNull AttachmentType<@NotNull BotWStaminaData>> botwStaminaData = attachmentTypes.register("botw_stamina",
			() -> AttachmentType.builder(h -> new BotWStaminaData())
					.serialize(BotWStaminaData.CODEC)
					.build());

	public final DeferredHolder<@NotNull AttachmentType<?>, @NotNull AttachmentType<@NotNull SimpleVesselContainer>> vesselContainer = attachmentTypes.register("vessel_container",
			() -> AttachmentType.builder(h -> new SimpleVesselContainer())
					.serialize(SimpleVesselContainer.CODEC)
					.copyOnDeath()
					.build());

	public final DeferredHolder<@NotNull AttachmentType<?>, @NotNull AttachmentType<@NotNull MovementState>> movementState = attachmentTypes.register("movement_state",
			() -> AttachmentType.builder(h -> new MovementState())
					.serialize(MovementState.CODEC)
					.build());

	public final DeferredHolder<@NotNull AttachmentType<?>, @NotNull AttachmentType<@NotNull Boolean>> movementInitialized = attachmentTypes.register("movement_init",
			() -> AttachmentType.builder(h -> false)
					.serialize(Codec.BOOL.fieldOf("initialized"))
					.build());

	public final DeferredHolder<@NotNull IngredientType<?>, @NotNull IngredientType<?>> waterBottle = ingredientTypes.register("water_bottle", WaterBottleIngredientType.INSTANCE::getType);

	public final DeferredHolder<BargainPreview.@NotNull Type<?>, BargainPreview.@NotNull Type<SimplePreview>> simplePreviewType = bargainPreviewTypes.register("simple",
			() -> SimplePreview.TYPE);

	public final DeferredHolder<BargainPreview.@NotNull Type<?>, BargainPreview.@NotNull Type<VesselPreview>> vesselPreviewType = bargainPreviewTypes.register("vessel",
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
		this.slotDisplays.register(eventBus);
		this.ingredientTypes.register(eventBus);
		this.bargainPreviewTypes.register(eventBus);

		ParagliderLootConditions.register(this.lootConditions);
		ParagliderConfigCondition.register(this.conditionSerializers);
		VesselSlotDisplay.register(this.slotDisplays);
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
	public @NotNull Holder<@NotNull Attribute> maxStamina() {
		return maxStamina;
	}
	public @NotNull Holder<@NotNull Attribute> staminaEfficiency() {
		return staminaEfficiency;
	}
	public @NotNull Holder<@NotNull Attribute> staminaRecovery() {
		return staminaRecovery;
	}
	public @NotNull Holder<@NotNull Attribute> paraglidingStaminaEfficiency() {
		return paraglidingStaminaEfficiency;
	}
	public @NotNull Holder<@NotNull Attribute> runningStaminaEfficiency() {
		return runningStaminaEfficiency;
	}
	public @NotNull Holder<@NotNull Attribute> swimmingStaminaEfficiency() {
		return swimmingStaminaEfficiency;
	}
	public @NotNull DataComponentType<@NotNull Unit> paraglidingFlagComponent() {
		return paraglidingFlagComponent.get();
	}
	public @NotNull CosmeticRecipe.Serializer cosmeticRecipeSerializer() {
		return cosmeticRecipe.get();
	}
	public @NotNull RecipeSerializer<@NotNull SimpleBargain> bargainRecipeSerializer() {
		return bargainRecipe.get();
	}
	public @NotNull RecipeType<@NotNull Bargain> bargainRecipeType() {
		return bargainRecipeType.get();
	}
	public @NotNull StructureType<@NotNull TarreyTownGoddessStatue> tarreyTownGoddessStatue() {
		return tarreyTownGoddessStatue.get();
	}
	public @NotNull StructureType<@NotNull NetherHornedStatue> netherHornedStatue() {
		return netherHornedStatue.get();
	}
	public @NotNull StructureType<@NotNull UndergroundHornedStatue> undergroundHornedStatue() {
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
	public @NotNull AttachmentType<@NotNull PlayerMovement> playerMovement() {
		return playerMovement.get();
	}
	public @NotNull AttachmentType<@NotNull BotWStaminaData> botwStaminaData() {
		return botwStaminaData.get();
	}
	public @NotNull AttachmentType<@NotNull SimpleVesselContainer> vesselContainer() {
		return vesselContainer.get();
	}
	public @NotNull AttachmentType<@NotNull MovementState> movementState() {
		return movementState.get();
	}
	public @NotNull AttachmentType<@NotNull Boolean> movementInitialized() {
		return movementInitialized.get();
	}
}
