package tictim.paraglider.loot;

import com.google.gson.JsonObject;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.item.ParagliderItem;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ParagliderModifier extends LootModifier{
	public final boolean dekuLeaf;

	public ParagliderModifier(ILootCondition[] conditionsIn){
		this(conditionsIn, false);
	}
	public ParagliderModifier(ILootCondition[] conditionsIn, boolean dekuLeaf){
		super(conditionsIn);
		this.dekuLeaf = dekuLeaf;
	}

	@Nonnull @Override protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context){
		ParagliderItem item = (dekuLeaf ? Contents.DEKU_LEAF : Contents.PARAGLIDER).get();
		ItemStack stack = new ItemStack(item);

		if(context.getRandom().nextBoolean()) item.setColor(stack, getRandomDyeColor(context.getRandom()));

		generatedLoot.add(stack);
		return generatedLoot;
	}

	/**
	 * @return Random two dyes mixed
	 */
	public static int getRandomDyeColor(Random random){
		return getMixedColor(DyeColor.byId(random.nextInt(16)), DyeColor.byId(random.nextInt(16)));
	}

	// stolen from IDyeableArmorItem#dyeItem(). Don't ask me what it does
	public static int getMixedColor(DyeColor dye1, DyeColor dye2){
		if(dye1==dye2) return dye1.getColorValue();
		float[] color1 = dye1.getColorComponentValues();
		float[] color2 = dye2.getColorComponentValues();

		int red1 = (int)(color1[0]*255);
		int red2 = (int)(color2[0]*255);
		int green1 = (int)(color1[1]*255);
		int green2 = (int)(color2[1]*255);
		int blue1 = (int)(color1[2]*255);
		int blue2 = (int)(color2[2]*255);
		int red = (red1+red2)/2;
		int green = (green1+green2)/2;
		int blue = (blue1+blue2)/2;

		float f3 = (Math.max(red1, Math.max(green1, blue1))+Math.max(red2, Math.max(green2, blue2)))/2f;
		float f4 = Math.max(red, Math.max(green, blue));
		red = (int)(red*f3/f4);
		green = (int)(green*f3/f4);
		blue = (int)(blue*f3/f4);

		return red<<16|green<<8|blue;
	}

	public static class Serializer extends GlobalLootModifierSerializer<ParagliderModifier>{
		@Override public ParagliderModifier read(ResourceLocation location, JsonObject object, ILootCondition[] lootConditions){
			return new ParagliderModifier(lootConditions, object.get("dekuLeaf").getAsBoolean());
		}
		@Override public JsonObject write(ParagliderModifier instance){
			JsonObject jsonObject = this.makeConditions(instance.conditions);
			jsonObject.addProperty("dekuLeaf", instance.dekuLeaf);
			return jsonObject;
		}
	}
}
