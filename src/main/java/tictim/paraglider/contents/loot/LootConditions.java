package tictim.paraglider.contents.loot;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.config.Cfg;
import tictim.paraglider.contents.Contents;

public enum LootConditions implements LootItemCondition {
	WITHER_DROPS_VESSEL,
	SPIRIT_ORB_LOOTS;

	@Override public @NotNull LootItemConditionType getType() {
		Contents contents = ParagliderMod.instance().getContents();
		return switch (this) {
			case WITHER_DROPS_VESSEL -> contents.witherDropsVesselConfigCondition.get();
			case SPIRIT_ORB_LOOTS -> contents.spiritOrbLootsConfigCondition.get();
		};
	}

	@Override public boolean test(@NotNull LootContext lootContext) {
		return switch (this) {
			case WITHER_DROPS_VESSEL -> Cfg.get().witherDropsVessel();
			case SPIRIT_ORB_LOOTS -> Cfg.get().spiritOrbLoots();
		};
	}
}
