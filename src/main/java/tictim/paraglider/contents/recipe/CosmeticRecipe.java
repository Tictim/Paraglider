package tictim.paraglider.contents.recipe;

import com.google.common.collect.Streams;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.stream.Stream;

@NullMarked
public class CosmeticRecipe extends NormalCraftingRecipe {
	public static final MapCodec<CosmeticRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
			CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Ingredient.CODEC.listOf(1, 8).fieldOf("reagents").forGetter(r -> r.reagents),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
	).apply(b, CosmeticRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
			CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.reagents,
			ItemStackTemplate.STREAM_CODEC, r -> r.result,
			CosmeticRecipe::new
	);

	public static final RecipeSerializer<CosmeticRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final Ingredient input;
	private final List<Ingredient> reagents;
	private final ItemStackTemplate result;

	public CosmeticRecipe(Recipe.CommonInfo commonInfo,
	                      CraftingRecipe.CraftingBookInfo bookInfo,
	                      Ingredient input, List<Ingredient> reagents,
	                      ItemStackTemplate result) {
		super(commonInfo, bookInfo);
		this.input = input;
		this.reagents = reagents;
		this.result = result;
	}

	@Override public boolean matches(CraftingInput input, Level level) {
		boolean inputSeen = false;
		boolean[] reagentsSeen = new boolean[this.reagents.size()];

		Loop:
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (this.input.test(stack) && !this.result.is(stack.getItem())) {
				if (inputSeen) return false;
				inputSeen = true;
				continue;
			}

			for (int j = 0; j < this.reagents.size(); j++) {
				if (reagentsSeen[j]) continue;
				if (this.reagents.get(j).test(stack)) {
					reagentsSeen[j] = true;
					continue Loop;
				}
			}

			return false;
		}

		for (boolean b : reagentsSeen) {
			if (!b) return false;
		}
		return inputSeen;
	}

	@Override public ItemStack assemble(CraftingInput input) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (this.input.test(stack) && !this.result.is(stack.getItem())) {
				return this.result.apply(stack.count(), stack.getComponentsPatch());
			}
		}

		return ItemStack.EMPTY;
	}

	@Override public List<RecipeDisplay> display() {
		return List.of(new ShapelessCraftingRecipeDisplay(
				Streams.concat(
						Stream.of(this.input.display()),
						this.reagents.stream().map(Ingredient::display)
				).toList(),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
		));
	}

	@Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		boolean inputSeen = false;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (!inputSeen && this.input.test(stack) && !this.result.is(stack.getItem())) {
				inputSeen = true;
				ItemStackTemplate rem = stack.getCraftingRemainder();
				list.set(i, rem != null ? rem.create() : ItemStack.EMPTY);
				continue;
			}

			list.set(i, stack.copyWithCount(1));
		}

		return list;
	}

	@Override public RecipeSerializer<? extends CosmeticRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.create(
				Streams.concat(
						Stream.of(this.input),
						this.reagents.stream()
				).toList()
		);
	}
}
