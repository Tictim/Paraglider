package tictim.paraglider.contents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tictim.paraglider.block.GoddessStatueBlock;
import tictim.paraglider.block.HornedStatueBlock;
import tictim.paraglider.contents.mobeffects.ExhaustedEffect;
import tictim.paraglider.contents.worldgen.NetherHornedStatue;
import tictim.paraglider.contents.worldgen.TarreyTownGoddessStatue;
import tictim.paraglider.contents.worldgen.UndergroundHornedStatue;
import tictim.paraglider.item.AntiVesselItem;
import tictim.paraglider.item.EssenceItem;
import tictim.paraglider.item.HeartContainerItem;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.item.SpiritOrbItem;
import tictim.paraglider.item.StaminaVesselItem;
import tictim.paraglider.loot.ParagliderModifier;
import tictim.paraglider.loot.SpiritOrbLootModifier;
import tictim.paraglider.loot.VesselLootModifier;
import tictim.paraglider.recipe.CosmeticRecipe;
import tictim.paraglider.recipe.bargain.SimpleStatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class Contents{
	private Contents(){}

	public static final CreativeModeTab GROUP = new CreativeModeTab(MODID){
		@Override public ItemStack makeIcon(){
			return new ItemStack(PARAGLIDER.get());
		}
	};

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
	public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, MODID);
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MODID);
	public static final DeferredRegister<StructureFeature<?>> STRUCTURE_FEATURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, MODID);

	public static final RecipeType<StatueBargain> STATUE_BARGAIN_RECIPE_TYPE = new RecipeType<>(){};

	private static BlockBehaviour.Properties statueProperties(){
		return Block.Properties.of(Material.STONE)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.strength(1.5f, 100f)
				.noOcclusion();
	}

	public static final RegistryObject<Block> GODDESS_STATUE = BLOCKS.register("goddess_statue",
			() -> new GoddessStatueBlock(statueProperties()));
	public static final RegistryObject<Block> KAKARIKO_GODDESS_STATUE = BLOCKS.register("kakariko_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties(),
					new TranslatableComponent("tooltip.paraglider.kakariko_goddess_statue.0")
							.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));
	public static final RegistryObject<Block> GORON_GODDESS_STATUE = BLOCKS.register("goron_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties().lightLevel(value -> 15),
					new TranslatableComponent("tooltip.paraglider.goron_goddess_statue.0")
							.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));
	public static final RegistryObject<Block> RITO_GODDESS_STATUE = BLOCKS.register("rito_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties(),
					new TranslatableComponent("tooltip.paraglider.rito_goddess_statue.0")
							.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))));
	public static final RegistryObject<Block> HORNED_STATUE = BLOCKS.register("horned_statue",
			() -> new HornedStatueBlock(statueProperties()));

	public static final RegistryObject<ParagliderItem> PARAGLIDER = ITEMS.register("paraglider", () -> new ParagliderItem(0xA65955));
	public static final RegistryObject<ParagliderItem> DEKU_LEAF = ITEMS.register("deku_leaf", () -> new ParagliderItem(0x3FB53F));
	public static final RegistryObject<Item> HEART_CONTAINER = ITEMS.register("heart_container", HeartContainerItem::new);
	public static final RegistryObject<Item> STAMINA_VESSEL = ITEMS.register("stamina_vessel", StaminaVesselItem::new);
	public static final RegistryObject<Item> SPIRIT_ORB = ITEMS.register("spirit_orb", () -> new SpiritOrbItem(new Item.Properties().rarity(Rarity.UNCOMMON).tab(GROUP)));
	public static final RegistryObject<Item> ANTI_VESSEL = ITEMS.register("anti_vessel", () -> new AntiVesselItem(new Item.Properties().rarity(Rarity.EPIC).tab(GROUP)));
	public static final RegistryObject<Item> ESSENCE = ITEMS.register("essence", () -> new EssenceItem(new Item.Properties().rarity(Rarity.RARE).tab(GROUP)));
	public static final RegistryObject<BlockItem> GODDESS_STATUE_ITEM = ITEMS.register("goddess_statue", () -> new BlockItem(GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).tab(GROUP)));
	public static final RegistryObject<BlockItem> KAKARIKO_GODDESS_STATUE_ITEM = ITEMS.register("kakariko_goddess_statue", () -> new BlockItem(KAKARIKO_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).tab(GROUP)));
	public static final RegistryObject<BlockItem> GORON_GODDESS_STATUE_ITEM = ITEMS.register("goron_goddess_statue", () -> new BlockItem(GORON_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).tab(GROUP)));
	public static final RegistryObject<BlockItem> RITO_GODDESS_STATUE_ITEM = ITEMS.register("rito_goddess_statue", () -> new BlockItem(RITO_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).tab(GROUP)));
	public static final RegistryObject<BlockItem> HORNED_STATUE_ITEM = ITEMS.register("horned_statue", () -> new BlockItem(HORNED_STATUE.get(),
			new Item.Properties().rarity(Rarity.EPIC).tab(GROUP)));

	public static final RegistryObject<MobEffect> EXHAUSTED = EFFECTS.register("exhausted", ExhaustedEffect::new);

	public static final RegistryObject<ParagliderModifier.Serializer> PARAGLIDER_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("paraglider", ParagliderModifier.Serializer::new);
	public static final RegistryObject<SpiritOrbLootModifier.Serializer> SPIRIT_ORB_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("spirit_orb", SpiritOrbLootModifier.Serializer::new);
	public static final RegistryObject<VesselLootModifier.Serializer> VESSEL_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("vessel", VesselLootModifier.Serializer::new);

	public static final RegistryObject<CosmeticRecipe.Serializer> COSMETIC_RECIPE = RECIPE_SERIALIZERS.register("cosmetic", CosmeticRecipe.Serializer::new);
	public static final RegistryObject<SimpleStatueBargain.Serializer> STATUE_BARGAIN_RECIPE = RECIPE_SERIALIZERS.register("statue_bargain", SimpleStatueBargain.Serializer::new);

	public static final RegistryObject<MenuType<StatueBargainContainer>> GODDESS_STATUE_CONTAINER = CONTAINERS.register(
			"goddess_statue", () -> new MenuType<>(ModContainers::goddessStatue));
	public static final RegistryObject<MenuType<StatueBargainContainer>> HORNED_STATUE_CONTAINER = CONTAINERS.register(
			"horned_statue", () -> new MenuType<>(ModContainers::hornedStatue));

	public static final RegistryObject<Attribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina", () -> new RangedAttribute("max_stamina", 0, 0, Double.MAX_VALUE).setSyncable(true));

	public static final RegistryObject<StructureFeature<?>> UNDERGROUND_HORNED_STATUE = STRUCTURE_FEATURES.register("underground_horned_statue", UndergroundHornedStatue::new);
	public static final RegistryObject<StructureFeature<?>> NETHER_HORNED_STATUE = STRUCTURE_FEATURES.register("nether_horned_statue", NetherHornedStatue::new);
	public static final RegistryObject<StructureFeature<?>> TARREY_TOWN_GODDESS_STATUE = STRUCTURE_FEATURES.register("tarrey_town_goddess_statue", TarreyTownGoddessStatue::new);

	public static void registerEventHandlers(IEventBus eventBus){
		BLOCKS.register(eventBus);
		ITEMS.register(eventBus);
		EFFECTS.register(eventBus);
		CONTAINERS.register(eventBus);
		LOOT_MODIFIER_SERIALIZERS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		ATTRIBUTES.register(eventBus);
		STRUCTURE_FEATURES.register(eventBus);
	}

	@SubscribeEvent
	public static void registerRecipeType(RegistryEvent.Register<RecipeSerializer<?>> event){
		Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, "statue_bargain"), STATUE_BARGAIN_RECIPE_TYPE);
	}
}
