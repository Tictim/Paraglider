package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.utils.ParagliderUtils;

import java.util.List;

public class VesselLootModifier extends LootModifier{
	private final int count;

	public VesselLootModifier(ILootCondition[] conditionsIn){
		this(conditionsIn, 0);
	}
	public VesselLootModifier(ILootCondition[] conditionsIn, int count){
		super(conditionsIn);
		this.count = count;
	}

	@Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item!=null) generatedLoot.add(new ItemStack(item, count));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<VesselLootModifier>{
		@Override public VesselLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] lootConditions){
			return new VesselLootModifier(lootConditions, JSONUtils.getInt(object, "count"));
		}
		@Override public JsonObject write(VesselLootModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("count", instance.count);
			return jsonObject;
		}
	}
}
