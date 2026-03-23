package tictim.paraglider.contents.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.config.Cfg;

@NullMarked
public enum ParagliderLootConditions implements LootItemCondition {
	WITHER_DROPS_VESSEL("config_wither_drops_vessel"),
	ELDER_GUARDIAN_DROPS_SPIRIT_ORB("config_elder_guardian_drops_spirit_orb"),
	SPIRIT_ORB_LOOTS("config_spirit_orb_loots");

	private final MapCodec<ParagliderLootConditions> codec = MapCodec.unit(this);
	private final String id;

	ParagliderLootConditions(String id) {
		this.id = id;
	}

	@Override public MapCodec<? extends LootItemCondition> codec() {
		return codec;
	}

	@Override public boolean test(LootContext lootContext) {
		return switch (this) {
			case WITHER_DROPS_VESSEL -> Cfg.get().witherDropsVessel();
			case ELDER_GUARDIAN_DROPS_SPIRIT_ORB -> Cfg.get().elderGuardianDropsSpiritOrb();
			case SPIRIT_ORB_LOOTS -> Cfg.get().spiritOrbLoots();
		};
	}

	public static void register(DeferredRegister<MapCodec<? extends LootItemCondition>> register) {
		for (ParagliderLootConditions c : values()) {
			MapCodec<? extends LootItemCondition> codec = c.codec();
			register.register(c.id, () -> codec);
		}
	}
}
