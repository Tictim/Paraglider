package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.Contents;

import javax.annotation.Nonnull;
import java.util.List;

public class SpiritOrbLootModifier extends LootModifier{
	private final int count;

	public SpiritOrbLootModifier(LootItemCondition[] conditionsIn){
		this(conditionsIn, 0);
	}
	public SpiritOrbLootModifier(LootItemCondition[] conditionsIn, int count){
		super(conditionsIn);
		this.count = count;
	}

	@Nonnull @Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		if(ModCfg.enableSpiritOrbGens()) generatedLoot.add(new ItemStack(Contents.SPIRIT_ORB.get(), count));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<SpiritOrbLootModifier>{
		@Override public SpiritOrbLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] lootConditions){
			return new SpiritOrbLootModifier(lootConditions, GsonHelper.getAsInt(object, "count"));
		}
		@Override public JsonObject write(SpiritOrbLootModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("count", instance.count);
			return jsonObject;
		}
	}
}
