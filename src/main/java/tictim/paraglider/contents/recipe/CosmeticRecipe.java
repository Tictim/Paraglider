package tictim.paraglider.contents.recipe;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.contents.Contents;

import java.util.List;
import java.util.stream.Stream;

public class CosmeticRecipe implements CraftingRecipe {
	private final String group;
	private final CraftingBookCategory category;
	private final Ingredient input;
	private final List<Ingredient> reagents;
	private final TransmuteResult result;
	private @Nullable PlacementInfo placementInfoCache;

	public CosmeticRecipe(String group, CraftingBookCategory category,
	                      Ingredient input, List<Ingredient> reagents,
	                      TransmuteResult result) {
		this.group = group;
		this.category = category;
		this.input = input;
		this.reagents = reagents;
		this.result = result;
	}

	@Override public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		boolean inputSeen = false;
		boolean[] reagentsSeen = new boolean[this.reagents.size()];

		Loop:
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (this.input.test(stack) && !this.result.isResultUnchanged(stack)) {
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

	@Override public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.@NotNull Provider provider) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (this.input.test(stack) && !this.result.isResultUnchanged(stack)) {
				return this.result.apply(stack);
			}
		}

		return ItemStack.EMPTY;
	}

	@Override public @NotNull List<RecipeDisplay> display() {
		return List.of(new ShapelessCraftingRecipeDisplay(
				Streams.concat(
						Stream.of(this.input.display()),
						this.reagents.stream().map(Ingredient::display)
				).toList(),
				this.result.display(),
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
		));
	}

	@Override public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		boolean inputSeen = false;

		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (!inputSeen && this.input.test(stack) && !this.result.isResultUnchanged(stack)) {
				inputSeen = true;
				list.set(i, stack.getCraftingRemainder());
				continue;
			}

			list.set(i, stack.copyWithCount(1));
		}

		return list;
	}

	@Override public @NotNull String group() {
		return group;
	}

	@Override public @NotNull RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return Contents.get().cosmeticRecipeSerializer();
	}

	@Override public @NotNull PlacementInfo placementInfo() {
		if (this.placementInfoCache == null) {
			this.placementInfoCache = PlacementInfo.create(
					Streams.concat(
							Stream.of(this.input),
							this.reagents.stream()
					).toList()
			);
		}
		return this.placementInfoCache;
	}

	@Override public @NotNull CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<CosmeticRecipe> {
		private static final MapCodec<CosmeticRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(r -> r.category),
				Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
				Ingredient.CODEC.listOf(1, 8).fieldOf("reagents").forGetter(r -> r.reagents),
				TransmuteResult.CODEC.fieldOf("result").forGetter(r -> r.result)
		).apply(b, CosmeticRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, r -> r.group,
				CraftingBookCategory.STREAM_CODEC, r -> r.category,
				Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.reagents,
				TransmuteResult.STREAM_CODEC, r -> r.result,
				CosmeticRecipe::new
		);

		@Override public @NotNull MapCodec<CosmeticRecipe> codec() {
			return CODEC;
		}
		@Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, CosmeticRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
