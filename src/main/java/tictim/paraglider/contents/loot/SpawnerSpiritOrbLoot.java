package tictim.paraglider.contents.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.Contents;

public class SpawnerSpiritOrbLoot extends LootModifier {
	public static final MapCodec<SpawnerSpiritOrbLoot> CODEC = RecordCodecBuilder.mapCodec(b ->
			codecStart(b).apply(b, SpawnerSpiritOrbLoot::new));

	public SpawnerSpiritOrbLoot(@NotNull LootItemCondition @NotNull ... conditions) {
		super(conditions);
	}

	@Override protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
		if (FeatureCfg.get().enableSpiritOrbGens()) {
			double drops = Cfg.get().spawnerSpiritOrbDrops();
			if (drops > 0) {
				int count = (int)drops;
				double frac = drops % 1;
				if (frac > 0) {
					if (context.getRandom().nextDouble() < frac) count++;
				}

				if (count > 0) generatedLoot.add(new ItemStack(Contents.get().spiritOrb(), count));
			}
		}

		return generatedLoot;
	}

	@Override public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
