package tictim.paraglider.contents;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.block.GoddessStatueBlock;
import tictim.paraglider.block.HornedStatueBlock;
import tictim.paraglider.item.AntiVesselItem;
import tictim.paraglider.item.EssenceItem;
import tictim.paraglider.item.HeartContainerItem;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.item.SpiritOrbItem;
import tictim.paraglider.item.StaminaVesselItem;
import tictim.paraglider.loot.ItemModifier;
import tictim.paraglider.loot.ParagliderModifier;
import tictim.paraglider.recipe.CosmeticRecipe;
import tictim.paraglider.recipe.bargain.SimpleStatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;

import java.util.ArrayList;
import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

public final class Contents{
	private Contents(){}

	public static final ItemGroup GROUP = new ItemGroup(MODID){
		@Override public ItemStack createIcon(){
			return new ItemStack(PARAGLIDER.get());
		}
	};

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MODID);
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

	public static final IRecipeType<StatueBargain> STATUE_BARGAIN_RECIPE_TYPE = Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(MODID, "statue_bargain"), new IRecipeType<StatueBargain>(){});

	private static AbstractBlock.Properties statueProperties(){
		return Block.Properties.create(Material.ROCK)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(1.5f, 100f)
				.notSolid();
	}

	public static final RegistryObject<Block> GODDESS_STATUE = BLOCKS.register("goddess_statue",
			() -> new GoddessStatueBlock(statueProperties()));
	public static final RegistryObject<Block> KAKARIKO_GODDESS_STATUE = BLOCKS.register("kakariko_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties(),
					new TranslationTextComponent("tooltip.paraglider.kakariko_goddess_statue.0")
							.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY))));
	public static final RegistryObject<Block> GORON_GODDESS_STATUE = BLOCKS.register("goron_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties().setLightLevel(value -> 15),
					new TranslationTextComponent("tooltip.paraglider.goron_goddess_statue.0")
							.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY))));
	public static final RegistryObject<Block> RITO_GODDESS_STATUE = BLOCKS.register("rito_goddess_statue",
			() -> new GoddessStatueBlock(statueProperties(),
					new TranslationTextComponent("tooltip.paraglider.rito_goddess_statue.0")
							.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY))));
	public static final RegistryObject<Block> HORNED_STATUE = BLOCKS.register("horned_statue",
			() -> new HornedStatueBlock(statueProperties()));

	public static final RegistryObject<ParagliderItem> PARAGLIDER = ITEMS.register("paraglider", () -> new ParagliderItem(0xA65955));
	public static final RegistryObject<ParagliderItem> DEKU_LEAF = ITEMS.register("deku_leaf", () -> new ParagliderItem(0x3FB53F));
	public static final RegistryObject<Item> HEART_CONTAINER = ITEMS.register("heart_container", HeartContainerItem::new);
	public static final RegistryObject<Item> STAMINA_VESSEL = ITEMS.register("stamina_vessel", StaminaVesselItem::new);
	public static final RegistryObject<Item> SPIRIT_ORB = ITEMS.register("spirit_orb", () -> new SpiritOrbItem(new Item.Properties().rarity(Rarity.UNCOMMON).group(GROUP)));
	public static final RegistryObject<Item> ANTI_VESSEL = ITEMS.register("anti_vessel", () -> new AntiVesselItem(new Item.Properties().rarity(Rarity.EPIC).group(GROUP)));
	public static final RegistryObject<Item> ESSENCE = ITEMS.register("essence", () -> new EssenceItem(new Item.Properties().rarity(Rarity.RARE).group(GROUP)));
	public static final RegistryObject<BlockItem> GODDESS_STATUE_ITEM = ITEMS.register("goddess_statue", () -> new BlockItem(GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).group(GROUP)));
	public static final RegistryObject<BlockItem> KAKARIKO_GODDESS_STATUE_ITEM = ITEMS.register("kakariko_goddess_statue", () -> new BlockItem(KAKARIKO_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).group(GROUP)));
	public static final RegistryObject<BlockItem> GORON_GODDESS_STATUE_ITEM = ITEMS.register("goron_goddess_statue", () -> new BlockItem(GORON_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).group(GROUP)));
	public static final RegistryObject<BlockItem> RITO_GODDESS_STATUE_ITEM = ITEMS.register("rito_goddess_statue", () -> new BlockItem(RITO_GODDESS_STATUE.get(),
			new Item.Properties().rarity(Rarity.RARE).group(GROUP)));
	public static final RegistryObject<BlockItem> HORNED_STATUE_ITEM = ITEMS.register("horned_statue", () -> new BlockItem(HORNED_STATUE.get(),
			new Item.Properties().rarity(Rarity.EPIC).group(GROUP)));

	public static final RegistryObject<Effect> EXHAUSTED = EFFECTS.register("exhausted", () -> new Effect(EffectType.HARMFUL, 5926017){
		@Override public boolean shouldRender(EffectInstance effect){
			return false;
		}
		@Override public boolean shouldRenderInvText(EffectInstance effect){
			return false;
		}
		@Override public boolean shouldRenderHUD(EffectInstance effect){
			return false;
		}

		@Override public List<ItemStack> getCurativeItems(){
			return new ArrayList<>();
		}
	}.addAttributesModifier(Attributes.MOVEMENT_SPEED, "65ed2ca4-ceb3-4521-8552-73006dcba58d", -0.30, AttributeModifier.Operation.MULTIPLY_TOTAL)); // Slowness color

	public static final RegistryObject<ParagliderModifier.Serializer> PARAGLIDER_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("paraglider", ParagliderModifier.Serializer::new);
	public static final RegistryObject<ItemModifier.Serializer> ITEM_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("item", ItemModifier.Serializer::new);

	public static final RegistryObject<CosmeticRecipe.Serializer> COSMETIC_RECIPE = RECIPE_SERIALIZERS.register("cosmetic", CosmeticRecipe.Serializer::new);
	public static final RegistryObject<SimpleStatueBargain.Serializer> STATUE_BARGAIN_RECIPE = RECIPE_SERIALIZERS.register("statue_bargain", SimpleStatueBargain.Serializer::new);

	public static final RegistryObject<ContainerType<StatueBargainContainer>> GODDESS_STATUE_CONTAINER = CONTAINERS.register(
			"goddess_statue", () -> new ContainerType<>(ModContainers::goddessStatue));
	public static final RegistryObject<ContainerType<StatueBargainContainer>> HORNED_STATUE_CONTAINER = CONTAINERS.register(
			"horned_statue", () -> new ContainerType<>(ModContainers::hornedStatue));

	public static void registerEventHandlers(IEventBus eventBus){
		BLOCKS.register(eventBus);
		ITEMS.register(eventBus);
		EFFECTS.register(eventBus);
		CONTAINERS.register(eventBus);
		LOOT_MODIFIER_SERIALIZERS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
	}
}
