package tictim.paraglider.contents.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;

import java.util.stream.Stream;

@NullMarked
public enum WaterBottleIngredientType implements ICustomIngredient {
	INSTANCE;

	private final IngredientType<WaterBottleIngredientType> type = new IngredientType<>(
			MapCodec.unit(this),
			StreamCodec.unit(this)
	);

	@Override public boolean test(ItemStack stack) {
		return stack.is(Items.POTION) &&
				stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
	}

	@Override public Stream<Holder<Item>> items() {
		return Stream.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.POTION));
	}

	@Override public boolean isSimple() {
		return false;
	}

	@Override public IngredientType<?> getType() {
		return this.type;
	}

	@Override public SlotDisplay display() {
		return ParagliderUtils.stackDisplay(PotionContents.createItemStack(Items.POTION, Potions.WATER));
	}

	@Override public String toString() {
		return "WaterBottleIngredientType";
	}
}
