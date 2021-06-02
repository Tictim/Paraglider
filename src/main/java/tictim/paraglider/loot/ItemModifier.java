package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

public class ItemModifier extends LootModifier{
	private final Item item;
	private final int count;

	public ItemModifier(ILootCondition[] conditionsIn){
		this(conditionsIn, Items.AIR, 0);
	}
	public ItemModifier(ILootCondition[] conditionsIn, Item item, int count){
		super(conditionsIn);
		this.item = item;
		this.count = count;
	}

	@Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		generatedLoot.add(new ItemStack(item, count));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<ItemModifier>{
		@Override public ItemModifier read(ResourceLocation location, JsonObject object, ILootCondition[] lootConditions){
			//noinspection ConstantConditions
			return new ItemModifier(lootConditions,
					ForgeRegistries.ITEMS.getValue(new ResourceLocation(JSONUtils.getString(object, "item"))),
					JSONUtils.getInt(object, "count"));
		}
		@Override public JsonObject write(ItemModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(instance.item)).toString());
			jsonObject.addProperty("count", instance.count);
			return jsonObject;
		}
	}
}
