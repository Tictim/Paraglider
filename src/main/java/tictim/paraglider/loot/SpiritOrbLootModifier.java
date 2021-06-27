package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;

import java.util.List;

public class SpiritOrbLootModifier extends LootModifier{
	private final int count;

	public SpiritOrbLootModifier(ILootCondition[] conditionsIn){
		this(conditionsIn, 0);
	}
	public SpiritOrbLootModifier(ILootCondition[] conditionsIn, int count){
		super(conditionsIn);
		this.count = count;
	}

	@Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		if(ModCfg.enableSpiritOrbGens()) generatedLoot.add(new ItemStack(Contents.SPIRIT_ORB.get(), count));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<SpiritOrbLootModifier>{
		@Override public SpiritOrbLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] lootConditions){
			return new SpiritOrbLootModifier(lootConditions, JSONUtils.getInt(object, "count"));
		}
		@Override public JsonObject write(SpiritOrbLootModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("count", instance.count);
			return jsonObject;
		}
	}
}
