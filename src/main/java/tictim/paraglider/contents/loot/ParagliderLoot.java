package tictim.paraglider.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.Cfg.TotwCompatConfigOption;
import tictim.paraglider.contents.Contents;

import java.util.List;

import static tictim.paraglider.config.Cfg.TotwCompatConfigOption.*;

public class ParagliderLoot extends LootModifier {
	public static final MapCodec<ParagliderLoot> CODEC = RecordCodecBuilder.mapCodec(b ->
			b.group(Codec.BOOL.fieldOf("dekuLeaf").forGetter(m -> m.dekuLeaf))
					.and(codecStart(b).t1())
					.apply(b, ParagliderLoot::new));

	public final boolean dekuLeaf;

	public ParagliderLoot(boolean dekuLeaf, @NotNull LootItemCondition @NotNull ... conditions) {
		super(conditions);
		this.dekuLeaf = dekuLeaf;
	}

	@Override protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot,
	                                                                @NotNull LootContext context) {
		TotwCompatConfigOption configOption = Cfg.get().paragliderInTowersOfTheWild();
		if (configOption != DISABLE) {
			ItemStack stack = new ItemStack(
					configOption == DEKU_LEAF_ONLY || configOption != PARAGLIDER_ONLY && this.dekuLeaf ?
							Contents.get().dekuLeaf() : Contents.get().paraglider());
			RandomSource random = context.getRandom();
			if (random.nextBoolean()) {
				stack = DyedItemColor.applyDyes(stack, List.of(
						DyeItem.byColor(DyeColor.byId(random.nextInt(16))),
						DyeItem.byColor(DyeColor.byId(random.nextInt(16)))
				));
			}
			generatedLoot.add(stack);
		}
		return generatedLoot;
	}

	@Override public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
