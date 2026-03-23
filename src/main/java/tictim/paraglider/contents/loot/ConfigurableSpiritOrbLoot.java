package tictim.paraglider.contents.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.config.FeatureCfg;
import tictim.paraglider.contents.Contents;

import java.util.Locale;

@NullMarked
public class ConfigurableSpiritOrbLoot extends LootModifier {
	public static final MapCodec<ConfigurableSpiritOrbLoot> CODEC = RecordCodecBuilder.mapCodec(b ->
			b.group(StringRepresentable.fromEnum(Type::values).fieldOf("lootType").forGetter(m -> m.type))
					.and(codecStart(b).t1())
					.apply(b, ConfigurableSpiritOrbLoot::new));

	private final Type type;

	public ConfigurableSpiritOrbLoot(Type type, LootItemCondition... conditions) {
		super(conditions);
		this.type = type;
	}

	@Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (FeatureCfg.get().enableSpiritOrbGens()) {
			double drops = switch (this.type) {
				case SPAWNER -> Cfg.get().spawnerSpiritOrbDrops();
				case TRIAL -> Cfg.get().trialSpiritOrbDrops();
				case OMINOUS_TRIAL -> Cfg.get().ominousTrialSpiritOrbDrops();
			};

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

	@Override public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

	public enum Type implements StringRepresentable {
		SPAWNER,
		TRIAL,
		OMINOUS_TRIAL;

		private final String serializedName = name().toLowerCase(Locale.ROOT);

		@Override public String getSerializedName() {
			return this.serializedName;
		}
	}
}
