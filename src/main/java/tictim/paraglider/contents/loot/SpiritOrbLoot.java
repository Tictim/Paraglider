package tictim.paraglider.contents.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.Contents;

public class SpiritOrbLoot extends LootModifier {
	public static final MapCodec<SpiritOrbLoot> CODEC = RecordCodecBuilder.mapCodec(b ->
			b.group(Codec.INT.fieldOf("count").forGetter(m -> m.count))
					.and(codecStart(b).t1())
					.apply(b, SpiritOrbLoot::new));

	private final int count;

	public SpiritOrbLoot(int count, @NotNull LootItemCondition @NotNull ... conditions) {
		super(conditions);
		this.count = count;
	}

	@Override protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
		if (FeatureCfg.get().enableSpiritOrbGens()) generatedLoot.add(new ItemStack(Contents.get().spiritOrb(), count));
		return generatedLoot;
	}

	@Override public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
