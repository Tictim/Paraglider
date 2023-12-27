package tictim.paraglider.forge.contents;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.contents.block.GoddessStatueBlock;
import tictim.paraglider.contents.block.HornedStatueBlock;
import tictim.paraglider.contents.item.AntiVesselItem;
import tictim.paraglider.contents.item.EssenceItem;
import tictim.paraglider.contents.item.HeartContainerItem;
import tictim.paraglider.contents.item.ParagliderItem;
import tictim.paraglider.contents.item.SpiritOrbItem;
import tictim.paraglider.contents.item.StaminaVesselItem;
import tictim.paraglider.contents.recipe.CosmeticRecipe;
import tictim.paraglider.contents.recipe.SimpleBargainSerializer;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;
import tictim.paraglider.forge.contents.item.ForgeParagliderItem;
import tictim.paraglider.forge.contents.loot.LootConditions;
import tictim.paraglider.forge.contents.loot.ParagliderLoot;
import tictim.paraglider.forge.contents.loot.SpawnerSpiritOrbLoot;
import tictim.paraglider.forge.contents.loot.SpiritOrbLoot;
import tictim.paraglider.forge.contents.loot.VesselLoot;

import java.util.Locale;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.contents.CommonContents.*;

@SuppressWarnings("unused")
public final class ForgeContents implements Contents{
	private final DeferredRegister<Block> blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	private final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	private final DeferredRegister<RecipeSerializer<?>> recipeSerializers = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	private final DeferredRegister<RecipeType<?>> recipeTypes = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
	private final DeferredRegister<Attribute> attributes = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
	private final DeferredRegister<Codec<? extends IGlobalLootModifier>> loots = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
	private final DeferredRegister<LootItemConditionType> lootConditions = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
	private final DeferredRegister<StructureType<?>> structureTypes = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
	private final DeferredRegister<StructurePieceType> pieces = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
	private final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	private final DeferredRegister<Codec<? extends ICondition>> conditionSerializers = DeferredRegister.create(ForgeRegistries.Keys.CONDITION_SERIALIZERS, ParagliderAPI.MODID);

	private final RegistryObject<Block> goddessStatue = blocks.register("goddess_statue",
			() -> new GoddessStatueBlock(statueBlock()));
	private final RegistryObject<Block> kakarikoGoddessStatue = blocks.register("kakariko_goddess_statue",
			() -> new GoddessStatueBlock(statueBlock(), kakarikoStatueTooltip()));
	private final RegistryObject<Block> goronGoddessStatue = blocks.register("goron_goddess_statue",
			() -> new GoddessStatueBlock(statueBlock().lightLevel(value -> 15), goronStatueTooltip()));
	private final RegistryObject<Block> ritoGoddessStatue = blocks.register("rito_goddess_statue",
			() -> new GoddessStatueBlock(statueBlock(), ritoStatueTooltip()));
	private final RegistryObject<Block> hornedStatue = blocks.register("horned_statue",
			() -> new HornedStatueBlock(statueBlock()));

	private final RegistryObject<ParagliderItem> paraglider = items.register("paraglider", () -> new ForgeParagliderItem(PARAGLIDER_DEFAULT_COLOR));
	private final RegistryObject<ParagliderItem> dekuLeaf = items.register("deku_leaf", () -> new ForgeParagliderItem(DEKU_LEAF_DEFAULT_COLOR));
	private final RegistryObject<Item> heartContainer = items.register("heart_container", () -> new HeartContainerItem(rareItem()));
	private final RegistryObject<Item> staminaVessel = items.register("stamina_vessel", () -> new StaminaVesselItem(rareItem()));
	private final RegistryObject<Item> spiritOrb = items.register("spirit_orb", () -> new SpiritOrbItem(uncommonItem()));
	private final RegistryObject<Item> antiVessel = items.register("anti_vessel", () -> new AntiVesselItem(epicItem()));
	private final RegistryObject<Item> essence = items.register("essence", () -> new EssenceItem(rareItem()));
	private final RegistryObject<BlockItem> goddessStatueItem = items.register("goddess_statue", () -> new BlockItem(goddessStatue.get(), rareItem()));
	private final RegistryObject<BlockItem> kakarikoGoddessStatueItem = items.register("kakariko_goddess_statue", () -> new BlockItem(kakarikoGoddessStatue.get(), rareItem()));
	private final RegistryObject<BlockItem> goronGoddessStatueItem = items.register("goron_goddess_statue", () -> new BlockItem(goronGoddessStatue.get(), rareItem()));
	private final RegistryObject<BlockItem> ritoGoddessStatueItem = items.register("rito_goddess_statue", () -> new BlockItem(ritoGoddessStatue.get(), rareItem()));
	private final RegistryObject<BlockItem> hornedStatueItem = items.register("horned_statue", () -> new BlockItem(hornedStatue.get(), epicItem()));

	private final RegistryObject<CosmeticRecipe.Serializer> cosmeticRecipe = recipeSerializers.register("cosmetic", CosmeticRecipe.Serializer::new);
	private final RegistryObject<SimpleBargainSerializer.Simple> bargainRecipe = recipeSerializers.register("statue_bargain", SimpleBargainSerializer.Simple::new);

	private final RegistryObject<RecipeType<Bargain>> bargainRecipeType = recipeTypes.register("bargain",
			() -> RecipeType.simple(ParagliderAPI.id("bargain")));

	private final RegistryObject<Codec<ParagliderLoot>> paragliderLoot = loots.register("paraglider", () -> ParagliderLoot.CODEC);
	private final RegistryObject<Codec<SpiritOrbLoot>> spiritOrbLoot = loots.register("spirit_orb", () -> SpiritOrbLoot.CODEC);
	private final RegistryObject<Codec<VesselLoot>> vesselLoot = loots.register("vessel", () -> VesselLoot.CODEC);
	private final RegistryObject<Codec<SpawnerSpiritOrbLoot>> spawnerSpiritOrbLoot = loots.register("spawner_spirit_orb", () -> SpawnerSpiritOrbLoot.CODEC);

	public final RegistryObject<LootItemConditionType> witherDropsVesselConfigCondition = lootConditions.register("config_wither_drops_vessel",
			() -> new LootItemConditionType(LootConditions.WITHER_DROPS_VESSEL.codec()));
	public final RegistryObject<LootItemConditionType> spiritOrbLootsConfigCondition = lootConditions.register("config_spirit_orb_loots",
			() -> new LootItemConditionType(LootConditions.SPIRIT_ORB_LOOTS.codec()));

	private final RegistryObject<StructureType<TarreyTownGoddessStatue>> tarreyTownGoddessStatue = structureType("tarrey_town_goddess_statue", TarreyTownGoddessStatue.CODEC);
	private final RegistryObject<StructureType<NetherHornedStatue>> netherHornedStatue = structureType("nether_horned_statue", NetherHornedStatue.CODEC);
	private final RegistryObject<StructureType<UndergroundHornedStatue>> undergroundHornedStatue = structureType("underground_horned_statue", UndergroundHornedStatue.CODEC);

	private <T extends Structure> RegistryObject<StructureType<T>> structureType(String id, Codec<T> codec){
		return structureTypes.register(id, () -> () -> codec);
	}

	private final RegistryObject<StructurePieceType> tarreyTownGoddessStatuePiece = pieces.register("tarrey_town_goddess_statue", TarreyTownGoddessStatue::pieceType);
	private final RegistryObject<StructurePieceType> netherHornedStatuePiece = pieces.register("nether_horned_statue", NetherHornedStatue::pieceType);
	private final RegistryObject<StructurePieceType> undergroundHornedStatuePiece = pieces.register("underground_horned_statue", UndergroundHornedStatue::pieceType);

	private final RegistryObject<CreativeModeTab> tab = creativeTabs.register(MODID, () -> CreativeModeTab.builder()
			.icon(() -> new ItemStack(paraglider.get()))
			.title(Component.translatable("itemGroup."+MODID))
			.displayItems((features, out) -> {
				out.accept(paraglider.get());
				out.accept(dekuLeaf.get());
				out.accept(heartContainer.get());
				out.accept(staminaVessel.get());
				out.accept(spiritOrb.get());
				out.accept(antiVessel.get());
				out.accept(essence.get());
				out.accept(goddessStatue.get());
				out.accept(kakarikoGoddessStatue.get());
				out.accept(goronGoddessStatue.get());
				out.accept(ritoGoddessStatue.get());
				out.accept(hornedStatue.get());
			}).build());

	{
		for(ConfigConditionSerializer c : ConfigConditionSerializer.values()) {
			conditionSerializers.register(c.name().toLowerCase(Locale.ROOT), c::codec);
		}

		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		blocks.register(eventBus);
		items.register(eventBus);
		loots.register(eventBus);
		lootConditions.register(eventBus);
		recipeSerializers.register(eventBus);
		attributes.register(eventBus);
		recipeTypes.register(eventBus);
		structureTypes.register(eventBus);
		pieces.register(eventBus);
		creativeTabs.register(eventBus);
		conditionSerializers.register(eventBus);
	}

	@NotNull public DeferredRegister<Block> blocks(){
		return blocks;
	}

	@Override @NotNull public ParagliderItem paraglider(){
		return paraglider.get();
	}
	@Override @NotNull public ParagliderItem dekuLeaf(){
		return dekuLeaf.get();
	}
	@Override @NotNull public Item heartContainer(){
		return heartContainer.get();
	}
	@Override @NotNull public Item staminaVessel(){
		return staminaVessel.get();
	}
	@Override @NotNull public Item spiritOrb(){
		return spiritOrb.get();
	}
	@Override @NotNull public Item antiVessel(){
		return antiVessel.get();
	}
	@Override @NotNull public Item essence(){
		return essence.get();
	}
	@Override @NotNull public Block goddessStatue(){
		return goddessStatue.get();
	}
	@Override @NotNull public Block kakarikoGoddessStatue(){
		return kakarikoGoddessStatue.get();
	}
	@Override @NotNull public Block goronGoddessStatue(){
		return goronGoddessStatue.get();
	}
	@Override @NotNull public Block ritoGoddessStatue(){
		return ritoGoddessStatue.get();
	}
	@Override @NotNull public Block hornedStatue(){
		return hornedStatue.get();
	}
	@Override @NotNull public BlockItem goddessStatueItem(){
		return goddessStatueItem.get();
	}
	@Override @NotNull public BlockItem kakarikoGoddessStatueItem(){
		return kakarikoGoddessStatueItem.get();
	}
	@Override @NotNull public BlockItem goronGoddessStatueItem(){
		return goronGoddessStatueItem.get();
	}
	@Override @NotNull public BlockItem ritoGoddessStatueItem(){
		return ritoGoddessStatueItem.get();
	}
	@Override @NotNull public BlockItem hornedStatueItem(){
		return hornedStatueItem.get();
	}
	@Override @NotNull public CosmeticRecipe.Serializer cosmeticRecipeSerializer(){
		return cosmeticRecipe.get();
	}
	@Override @NotNull public RecipeSerializer<? extends Bargain> bargainRecipeSerializer(){
		return bargainRecipe.get();
	}
	@Override @NotNull public RecipeType<Bargain> bargainRecipeType(){
		return bargainRecipeType.get();
	}
	@Override @NotNull public StructureType<TarreyTownGoddessStatue> tarreyTownGoddessStatue(){
		return tarreyTownGoddessStatue.get();
	}
	@Override @NotNull public StructureType<NetherHornedStatue> netherHornedStatue(){
		return netherHornedStatue.get();
	}
	@Override @NotNull public StructureType<UndergroundHornedStatue> undergroundHornedStatue(){
		return undergroundHornedStatue.get();
	}
	@Override @NotNull public StructurePieceType tarreyTownGoddessStatuePiece(){
		return tarreyTownGoddessStatuePiece.get();
	}
	@Override @NotNull public StructurePieceType netherHornedStatuePiece(){
		return netherHornedStatuePiece.get();
	}
	@Override @NotNull public StructurePieceType undergroundHornedStatuePiece(){
		return undergroundHornedStatuePiece.get();
	}
}
