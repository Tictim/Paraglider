package tictim.paraglider.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LocalConfig implements Cfg{
	protected final ForgeConfigSpec spec;

	private final ForgeConfigSpec.BooleanValue ascendingWinds;
	private final ForgeConfigSpec.ConfigValue<List<? extends String>> windSources;
	@Nullable private BlockMatcher windSourcesParsed = null;

	private final ForgeConfigSpec.DoubleValue paraglidingSpeed;
	private final ForgeConfigSpec.IntValue paragliderDurability;

	private final ForgeConfigSpec.BooleanValue enderDragonDropsVessel;
	private final ForgeConfigSpec.BooleanValue witherDropsVessel;
	private final ForgeConfigSpec.BooleanValue raidGivesVessel;
	private final ForgeConfigSpec.IntValue spawnerSpiritOrbDrops;
	private final ForgeConfigSpec.BooleanValue spiritOrbLoots;

	private final ForgeConfigSpec.IntValue startingHearts;
	private final ForgeConfigSpec.IntValue maxHeartContainers;

	private final ForgeConfigSpec.IntValue maxStamina;
	private final ForgeConfigSpec.IntValue startingStamina;
	private final ForgeConfigSpec.IntValue maxStaminaVessels;

	private final ForgeConfigSpec.BooleanValue paraglidingConsumesStamina;
	private final ForgeConfigSpec.BooleanValue runningConsumesStamina;

	private final ForgeConfigSpec.EnumValue<TotwCompatConfigOption> paragliderInTowersOfTheWild;

	public LocalConfig(){
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		ascendingWinds = b.comment("Fire will float you upward.").define("ascendingWinds", true);
		windSources = b.comment("""
						You can customize which block produces wind.
						Write each blockstate to one of this format:
						  [block ID]   (Matches all state of the block)
						  [block ID]#[property1=value],[property2=value],[property3=value]   (Matches state of the block that has specified properties)
						  #[Tag ID]   (Matches all blocks with the tag)
						Same property cannot be specified multiple times. Wind sources with any invalid part will be excluded.""")
				.defineListAllowEmpty(Collections.singletonList("windSources"),
						() -> ImmutableList.of("fire",
								"soul_fire",
								"campfire#lit=true",
								"soul_campfire#lit=true"),
						o -> true);

		paraglidingSpeed = b.comment("""
						Multiplier to horizontal movement speed while paragliding.
						Value of 0.5 means 50% of the speed, 2.0 means two times the speed and so forth.""")
				.defineInRange("paraglidingSpeed", 1.0, 0.2, 10);
		paragliderDurability = b.comment("Durability of Paragliders. Set to zero to disable durability.")
				.defineInRange("paragliderDurability", 0, 0, Integer.MAX_VALUE);

		b.push("spiritOrbs");
		enderDragonDropsVessel = b.comment("If true, Ender Dragon will drop heart container(stamina vessel if heart container is disabled) upon death.").
				define("enderDragonDropsVessel", true);
		witherDropsVessel = b.comment("If true, Wither will drop heart container(stamina vessel if heart container is disabled) upon death.")
				.define("enderDragonDropsVessel", true);
		raidGivesVessel = b.comment("If true, Raids will give heart container(stamina vessel if heart container is disabled) upon victory.")
				.define("raidGivesVessel", true);
		spawnerSpiritOrbDrops = b.comment("Amount of Spirit Orbs dropped from spawners.")
				.defineInRange("spawnerSpiritOrbDrops", 2, 0, 64);
		spiritOrbLoots = b.comment("""
						If true, various types of chest will have chances of having Spirit Orbs inside.
						Does not change contents of already generated chests.""")
				.define("spiritOrbLoots", true);
		b.pop();

		b.push("vessels");
		startingHearts = b.comment("Starting health points measured in number of hearts.").defineInRange("startingHearts", 10, 1, 512);
		maxHeartContainers = b.comment("""
						Maximum amount of Heart Containers one player can consume.
						Do note that the maximum health point is capped at value of 1024 (or 512 hearts) by Minecraft's default
						attribute system; without modifying these limits, Heart Containers won't give you extra hearts beyond that.""")
				.defineInRange("maxHeartContainers", 20, 0, 512);

		maxStamina = b.comment("Maximum amount of stamina Player can get. Do note that one third of this value is equal to one stamina wheel.")
				.defineInRange("maxStamina", 3000, 0, Integer.MAX_VALUE);
		startingStamina = b.comment("""
						Amount of stamina Player starts with. Values higher than maxStamina doesn't work.
						If you want to make starting stamina displayed as one full stamina wheel, this value should be one third of maxStamina.""")
				.defineInRange("startingStamina", 1000, 0, Integer.MAX_VALUE);
		maxStaminaVessels = b.comment("Stamina Vessels players need to obtain max out stamina. More vessels means lesser stamina increase per vessel.")
				.defineInRange("maxStaminaVessels", 10, 0, Integer.MAX_VALUE);
		b.pop();

		b.push("stamina");
		paraglidingConsumesStamina = b.comment("Paragliding will consume stamina.").define("paraglidingConsumesStamina", true);
		runningConsumesStamina = b.comment("Certain non-paragliding actions, such as running and swimming, will consume stamina.")
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

	@Override public boolean ascendingWinds(){
		return get(spec, ascendingWinds);
	}
	@Override @NotNull public BlockMatcher windSourceMatcher(){
		return windSourcesParsed==null ? BlockMatcher.empty() : windSourcesParsed;
	}
	@Override public double paraglidingSpeed(){
		return get(spec, paraglidingSpeed);
	}
	@Override public int paragliderDurability(){
		return get(spec, paragliderDurability);
	}
	@Override public boolean enderDragonDropsVessel(){
		return get(spec, enderDragonDropsVessel);
	}
	@Override public boolean witherDropsVessel(){
		return get(spec, witherDropsVessel);
	}
	@Override public boolean raidGivesVessel(){
		return get(spec, raidGivesVessel);
	}
	@Override public int spawnerSpiritOrbDrops(){
		return get(spec, spawnerSpiritOrbDrops);
	}
	@Override public boolean spiritOrbLoots(){
		return get(spec, spiritOrbLoots);
	}
	@Override public int startingHearts(){
		return get(spec, startingHearts);
	}
	@Override public int maxHeartContainers(){
		return get(spec, maxHeartContainers);
	}
	@Override public int maxStamina(){
		return get(spec, maxStamina);
	}
	@Override public int startingStamina(){
		return get(spec, startingStamina);
	}
	@Override public int maxStaminaVessels(){
		return get(spec, maxStaminaVessels);
	}
	@Override public boolean paraglidingConsumesStamina(){
		return get(spec, paraglidingConsumesStamina);
	}
	@Override public boolean runningConsumesStamina(){
		return get(spec, runningConsumesStamina);
	}
	@Override @NotNull public TotwCompatConfigOption paragliderInTowersOfTheWild(){
		return get(spec, paragliderInTowersOfTheWild);
	}

	@NotNull private static <T> T get(@NotNull ForgeConfigSpec spec, @NotNull ForgeConfigSpec.ConfigValue<T> val){
		return spec.isLoaded() ? val.get() : val.getDefault();
	}

	protected void reloadWindSources(){
		BlockMatcher.Result result = BlockMatcher.parse(this.windSources.get());
		result.printErrors();
		this.windSourcesParsed = result.result();
	}
}
