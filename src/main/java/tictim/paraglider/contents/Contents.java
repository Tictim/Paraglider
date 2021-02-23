package tictim.paraglider.contents;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.item.HeartContainerItem;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.item.SpiritOrbItem;
import tictim.paraglider.item.StaminaVesselItem;

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

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	private static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);

	public static final RegistryObject<Block> HORNED_STATUE = BLOCKS.register("horned_statue",
			() -> new HornedStatueBlock(Block.Properties.create(Material.ROCK)
					.sound(SoundType.STONE)
					.harvestTool(ToolType.PICKAXE)
					.hardnessAndResistance(1.5f, 100f)
					.notSolid()));

	public static final RegistryObject<Item> PARAGLIDER = ITEMS.register("paraglider", () -> new ParagliderItem(0xA65955));
	public static final RegistryObject<Item> DEKU_LEAF = ITEMS.register("deku_leaf", () -> new ParagliderItem(0x3FB53F));
	public static final RegistryObject<Item> HEART_CONTAINER = ITEMS.register("heart_container", HeartContainerItem::new);
	public static final RegistryObject<Item> STAMINA_VESSEL = ITEMS.register("stamina_vessel", StaminaVesselItem::new);
	public static final RegistryObject<Item> SPIRIT_ORB = ITEMS.register("spirit_orb", () -> new SpiritOrbItem(new Item.Properties().rarity(Rarity.UNCOMMON).group(GROUP)));
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

	public static void registerEventHandlers(IEventBus eventBus){
		BLOCKS.register(eventBus);
		ITEMS.register(eventBus);
		EFFECTS.register(eventBus);
	}
}
