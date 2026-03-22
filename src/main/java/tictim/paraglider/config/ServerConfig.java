package tictim.paraglider.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public class ServerConfig implements Cfg {
	public final ModConfigSpec spec;

	private final ModConfigSpec.BooleanValue updraft;

	private final ModConfigSpec.DoubleValue paraglidingSpeed;
	private final ModConfigSpec.IntValue paragliderDurability;

	private final ModConfigSpec.BooleanValue enderDragonDropsVessel;
	private final ModConfigSpec.BooleanValue enderDragonVesselSpawnsOnPodium;
	private final ModConfigSpec.BooleanValue witherDropsVessel;
	private final ModConfigSpec.BooleanValue raidGivesVessel;
	private final ModConfigSpec.BooleanValue elderGuardianDropsSpiritOrb;
	private final ModConfigSpec.DoubleValue spawnerSpiritOrbDrops;
	private final ModConfigSpec.DoubleValue trialSpiritOrbDrops;
	private final ModConfigSpec.DoubleValue ominousTrialSpiritOrbDrops;
	private final ModConfigSpec.BooleanValue spiritOrbLoots;

	private final ModConfigSpec.IntValue startingHearts;
	private final ModConfigSpec.IntValue maxHeartContainers;

	private final ModConfigSpec.IntValue startingStamina;
	private final ModConfigSpec.IntValue maxStaminaVessels;
	private final ModConfigSpec.IntValue staminaIncreasePerVessel;

	private final ModConfigSpec.BooleanValue paraglidingConsumesStamina;
	private final ModConfigSpec.BooleanValue runningAndSwimmingConsumesStamina;

	private final ModConfigSpec.EnumValue<TotwCompatConfigOption> paragliderInTowersOfTheWild;

	public ServerConfig() {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();
		updraft = b.comment("""
						If true, wind sources will generate updrafts that interact with Paraglider.
						Wind sources and strength (i.e. height) of the updraft created can be adjusted with datapacks.
						See online docs for more information: (work in progress)""")
				.define("updraft", true);

		paraglidingSpeed = b.comment("""
						Multiplier to horizontal movement speed while paragliding.
						Value of 0.5 means 50% of the speed, 2.0 means two times the speed and so forth.""")
				.defineInRange("paraglidingSpeed", 1.0, 0.2, 10);
		paragliderDurability = b.comment("Durability of Paragliders. Set to zero to disable durability.")
				.defineInRange("paragliderDurability", 0, 0, Integer.MAX_VALUE);

		b.push("spiritOrbs");
		enderDragonDropsVessel = b.comment("If true, Ender Dragon will drop heart container(stamina vessel if heart container is disabled) upon death.")
				.define("enderDragonDropsVessel", true);
		enderDragonVesselSpawnsOnPodium = b.comment("""
						If true, heart container/stamina vessel dropped by Ender Dragon will spawn on top of the end \
						podium (the ending portal).
						If false, it will be instead given directly to players.
						This option does not change the amount of vessels given to each player. \
						Intended as a compatibility feature for mods that change end podium location.""")
				.define("enderDragonVesselSpawnsOnPodium", true);
		witherDropsVessel = b.comment("If true, Wither will drop heart container(stamina vessel if heart container is disabled) upon death.")
				.define("witherDropsVessel", true);
		raidGivesVessel = b.comment("If true, Raids will give heart container(stamina vessel if heart container is disabled) upon victory.")
				.define("raidGivesVessel", true);
		elderGuardianDropsSpiritOrb = b.comment("If true, Elder Guardian will drop a Spirit Orb upon death.")
				.define("elderGuardianDropsSpiritOrb", true);
		spawnerSpiritOrbDrops = b.comment("Amount of Spirit Orbs dropped from spawners. Fractional values are treated as a chanced drop," +
						" in addition to whole values which is guaranteed to drop.")
				.defineInRange("spawnerSpiritOrbDrops", 1.0, 0, 64);
		trialSpiritOrbDrops = b.comment("Amount of Spirit Orbs dropped from completing trial. Fractional values are treated as a chanced drop, in addition " +
						"to whole values which is guaranteed to drop.")
				.defineInRange("trialSpiritOrbDrops", 2.0, 0, 64);
		ominousTrialSpiritOrbDrops = b.comment("Amount of Spirit Orbs dropped from completing ominous trial. Fractional values are treated as a chanced drop, in " +
						"addition to whole values which is guaranteed to drop.")
				.defineInRange("ominousTrialSpiritOrbDrops", 4.0, 0, 64);
		spiritOrbLoots = b.comment("""
						If true, various types of chest will have chances of having Spirit Orbs inside.
						Does not change contents of already generated chests.""")
				.define("spiritOrbLoots", true);
		b.pop();

		b.push("vessels");
		startingHearts = b.comment("Starting health points measured in number of hearts.")
				.defineInRange("startingHearts", 10, 1, Integer.MAX_VALUE);
		maxHeartContainers = b.comment("""
						Maximum amount of Heart Containers one player can consume.
						Do note that the maximum health point is capped at value of 1024 (or 512 hearts) by Minecraft's default
						attribute system; without modifying these limits, Heart Containers won't give you extra hearts beyond that.""")
				.defineInRange("maxHeartContainers", 20, 0, Integer.MAX_VALUE);

		startingStamina = b.comment("Amount of stamina players start with. One full stamina wheel is equivalent to 1000 stamina.")
				.defineInRange("startingStamina", 1000, 0, Integer.MAX_VALUE);
		maxStaminaVessels = b.comment("Maximum amount of Stamina Vessels one player can consume. Higher value = higher maximum stamina.")
				.defineInRange("maxStaminaVessels", 10, 0, Integer.MAX_VALUE);
		staminaIncreasePerVessel = b.comment("Stamina increase per vessel. One full stamina wheel is equivalent to 1000 stamina.")
				.defineInRange("staminaIncreasePerVessel", 200, 0, Integer.MAX_VALUE);
		b.pop();

		b.push("stamina");
		paraglidingConsumesStamina = b.comment("Paragliding will consume stamina. Run /paraglider reloadPlayerStates after change.")
				.define("paraglidingConsumesStamina", true);
		runningAndSwimmingConsumesStamina = b.comment("Certain non-paragliding actions, such as running and swimming, will consume stamina. Run /paraglider reloadPlayerStates after change.")
				.define("runningAndSwimmingConsumesStamina", false);
		b.pop();

		paragliderInTowersOfTheWild = b.comment("""
						Configurable option for Towers of the Wild compat feature. Can be ignored if Towers of the Wild is not installed.
						DEFAULT: Default option, spawn Deku Leaf in ocean tower chests and Paraglider in normal tower chests
						DISABLE: Don't spawn anything
						PARAGLIDER_ONLY: Spawn paraglider in both ocean and normal tower chests
						DEKU_LEAF_ONLY: Spawn deku leaf in both ocean and normal tower chests, like a boss""")
				.defineEnum("paragliderInTowersOfTheWild", TotwCompatConfigOption.DEFAULT);

		spec = b.build();
	}

	@Override public boolean updraft() {
		return get(spec, updraft);
	}
	@Override public double paraglidingSpeed() {
		return get(spec, paraglidingSpeed);
	}
	@Override public int paragliderDurability() {
		return get(spec, paragliderDurability);
	}
	@Override public boolean enderDragonDropsVessel() {
		return get(spec, enderDragonDropsVessel);
	}
	@Override public boolean enderDragonVesselSpawnsOnPodium() {
		return get(spec, enderDragonVesselSpawnsOnPodium);
	}
	@Override public boolean witherDropsVessel() {
		return get(spec, witherDropsVessel);
	}
	@Override public boolean raidGivesVessel() {
		return get(spec, raidGivesVessel);
	}
	@Override public boolean elderGuardianDropsSpiritOrb() {
		return get(spec, elderGuardianDropsSpiritOrb);
	}
	@Override public double spawnerSpiritOrbDrops() {
		return get(spec, spawnerSpiritOrbDrops);
	}
	@Override public double trialSpiritOrbDrops() {
		return get(spec, trialSpiritOrbDrops);
	}
	@Override public double ominousTrialSpiritOrbDrops() {
		return get(spec, ominousTrialSpiritOrbDrops);
	}
	@Override public boolean spiritOrbLoots() {
		return get(spec, spiritOrbLoots);
	}
	@Override public int startingHearts() {
		return get(spec, startingHearts);
	}
	@Override public int maxHeartContainers() {
		return get(spec, maxHeartContainers);
	}
	@Override public int startingStamina() {
		return get(spec, startingStamina);
	}
	@Override public int maxStaminaVessels() {
		return get(spec, maxStaminaVessels);
	}
	@Override public int staminaIncreasePerVessel() {
		return get(spec, staminaIncreasePerVessel);
	}
	@Override public boolean paraglidingConsumesStamina() {
		return get(spec, paraglidingConsumesStamina);
	}
	@Override public boolean runningConsumesStamina() {
		return get(spec, runningAndSwimmingConsumesStamina);
	}
	@Override public @NotNull TotwCompatConfigOption paragliderInTowersOfTheWild() {
		return get(spec, paragliderInTowersOfTheWild);
	}

	private static <T> @NotNull T get(@NotNull ModConfigSpec spec, @NotNull ModConfigSpec.ConfigValue<T> val) {
		return spec.isLoaded() ? val.get() : val.getDefault();
	}
}
