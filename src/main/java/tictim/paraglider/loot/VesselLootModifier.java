package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.utils.ParagliderUtils;

import javax.annotation.Nonnull;

public class VesselLootModifier extends LootModifier{
	private final int count;

	public VesselLootModifier(LootItemCondition[] conditionsIn){
		this(conditionsIn, 0);
	}
	public VesselLootModifier(LootItemCondition[] conditionsIn, int count){
		super(conditionsIn);
		this.count = count;
	}

	@Nonnull @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context){
		Item item = ParagliderUtils.getAppropriateVessel();
		if(item!=null) generatedLoot.add(new ItemStack(item, count));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<VesselLootModifier>{
		@Override public VesselLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions){
			return new VesselLootModifier(lootConditions, GsonHelper.getAsInt(object, "count"));
		}
		@Override public JsonObject write(VesselLootModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("count", instance.count);
			return jsonObject;
		}
	}
}
