package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.item.ParagliderItem;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ParagliderModifier extends LootModifier{
	public final boolean dekuLeaf;

	public ParagliderModifier(LootItemCondition[] conditionsIn){
		this(conditionsIn, false);
	}
	public ParagliderModifier(LootItemCondition[] conditionsIn, boolean dekuLeaf){
		super(conditionsIn);
		this.dekuLeaf = dekuLeaf;
	}

	@Nonnull @Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		ConfigOption configOption = ModCfg.paragliderInTowersOfTheWild();
		if(configOption!=ConfigOption.DISABLE){
			ParagliderItem item = (configOption==ConfigOption.DEKU_LEAF_ONLY||(configOption!=ConfigOption.PARAGLIDER_ONLY&&dekuLeaf) ?
					Contents.DEKU_LEAF : Contents.PARAGLIDER).get();
			ItemStack stack = new ItemStack(item);
			if(context.getRandom().nextBoolean()){
				stack = DyeableLeatherItem.dyeArmor(stack, Arrays.asList(
						DyeItem.byColor(DyeColor.byId(context.getRandom().nextInt(16))),
						DyeItem.byColor(DyeColor.byId(context.getRandom().nextInt(16)))));
			}
			generatedLoot.add(stack);
		}
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<ParagliderModifier>{
		@Override public ParagliderModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions){
			return new ParagliderModifier(lootConditions, object.get("dekuLeaf").getAsBoolean());
		}
		@Override public JsonObject write(ParagliderModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("dekuLeaf", instance.dekuLeaf);
			return jsonObject;
		}
	}

	public enum ConfigOption{
		/**
		 * Default option, spawn Deku Leaf in ocean tower chests and Paraglider in normal tower chests
		 */
		DEFAULT,
		/**
		 * Don't spawn anything
		 */
		DISABLE,
		/**
		 * Spawn paraglider in both ocean and normal tower chests
		 */
		PARAGLIDER_ONLY,
		/**
		 * Spawn deku leaf in both ocean and normal tower chests, like a boss
		 */
		DEKU_LEAF_ONLY
	}
}
