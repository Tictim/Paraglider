package tictim.paraglider;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.capabilities.PlayerState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public final class ModCfg{
	private ModCfg(){}

	private static BooleanValue ascendingWinds;
	private static BooleanValue paraglidingConsumesStamina;
	private static BooleanValue runningConsumesStamina;
	private static ConfigValue<List<? extends String>> windSources;
	private static Map<Block, Predicate<BlockState>> windSourcesParsed;

	private static DoubleValue paraglidingSpeed;
	private static IntValue paragliderDurability;

	private static BooleanValue enderDragonDropsVessel;
	private static BooleanValue raidGivesVessel;

	private static IntValue startingHearts;
	private static IntValue maxHeartContainers;

	private static IntValue maxStamina;
	private static IntValue startingStamina;
	private static IntValue maxStaminaVessels;

	private static BooleanValue enableSpiritOrbGens;
	private static BooleanValue enableHeartContainers;
	private static BooleanValue enableStaminaVessels;
	private static BooleanValue enableStructures;

	private static BooleanValue debugPlayerMovement;
	private static BooleanValue traceMovementPacket;
	private static BooleanValue traceParaglidingPacket;
	private static BooleanValue traceVesselPacket;

	public static boolean ascendingWinds(){
		return ascendingWinds.get();
	}
	public static boolean paraglidingConsumesStamina(){
		return paraglidingConsumesStamina.get();
	}
	public static boolean runningConsumesStamina(){
		return runningConsumesStamina.get();
	}

	public static boolean isWindSource(BlockState state){
		Predicate<BlockState> p = windSourcesParsed.get(state.getBlock());
		return p!=null&&p.test(state);
	}

	public static double paraglidingSpeed(){
		return paraglidingSpeed.get();
	}
	public static int paragliderDurability(){
		return paragliderDurability.get();
	}

	public static boolean enderDragonDropsVessel(){
		return enderDragonDropsVessel.get();
	}
	public static boolean raidGivesVessel(){
		return raidGivesVessel.get();
	}

	public static int startingHearts(){
		return startingHearts.get();
	}
	public static int maxHeartContainers(){
		return maxHeartContainers.get();
	}

	public static int maxStamina(){
		return maxStamina.get();
	}
	public static int startingStamina(){
		return Math.min(maxStamina(), startingStamina.get());
	}
	public static int maxStaminaVessels(){
		return maxStaminaVessels.get();
	}

	public static int maxStamina(int staminaVessels){
		int maxStaminaVessels = maxStaminaVessels();
		int startingStamina = startingStamina();
		if(maxStaminaVessels<=0) return startingStamina;
		if(maxStaminaVessels<=staminaVessels) maxStamina();
		return startingStamina+(int)((double)staminaVessels/maxStaminaVessels*(maxStamina()-startingStamina));
	}

	public static boolean enableSpiritOrbGens(){
		return enableSpiritOrbGens.get();
	}
	public static boolean enableHeartContainers(){
		return enableHeartContainers.get();
	}
	public static boolean enableStaminaVessels(){
		return enableStaminaVessels.get();
	}
	public static boolean enableStructures(){
		return enableStructures.get();
	}

	public static boolean debugPlayerMovement(){
		return debugPlayerMovement.get();
	}
	public static boolean traceMovementPacket(){
		return traceMovementPacket.get();
	}
	public static boolean traceParaglidingPacket(){
		return traceParaglidingPacket.get();
	}
	public static boolean traceVesselPacket(){
		return traceVesselPacket.get();
	}

	public static void init(){
		ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
		ascendingWinds = server.comment("Fire will float you upward.").define("ascendingWinds", true);
		windSources = server.comment("You can customize which block produces wind.\n"+
				"Write each blockstate to one of this format:\n"+
				"  [block ID]   (Matches all state of the block)\n"+
				"  [block ID]#[property1=value],[property2=value],[property3=value]   (Matches state of the block that has specified properties)\n"+
				"Same property cannot be specified multiple times. Wind sources with any invalid part will be excluded.")
				.defineListAllowEmpty(Collections.singletonList("windSources"),
						() -> ImmutableList.of("fire",
								"campfire#lit=true",
								"soul_campfire#lit=true"),
						o -> true);

		paraglidingSpeed = server.comment("Horizontal movement speed while paragliding.").defineInRange("paraglidingSpeed", 1.0, 0.2, 10);
		paragliderDurability = server.comment("Durability of Paragliders. Set to zero to disable durability.").defineInRange("paragliderDurability", 0, 0, Integer.MAX_VALUE);

		server.push("spiritOrbs");
		enderDragonDropsVessel = server.comment("If true, Ender Dragon will drop heart container(stamina vessel if heart container is disabled) upon death.").define("enderDragonDropsVessel", true);
		raidGivesVessel = server.comment("If true, Raids will give heart container(stamina vessel if heart container is disabled) upon victory.").define("raidGivesVessel", true);
		server.pop();

		server.push("vessels");
		startingHearts = server.comment("Starting health points.").defineInRange("startingHearts", 10, 1, 512);
		maxHeartContainers = server.comment("Maximum amount of Heart Containers one player can consume.\n"+
				"Do note that the maximum health point is capped at 1024 (512 hearts).").defineInRange("maxHeartContainers", 20, 0, 512);

		maxStamina = server.comment("Maximum amount of stamina Player can get. Do note that one third of this value is equal to one stamina wheel.")
				.defineInRange("maxStamina", 3000, 0, Integer.MAX_VALUE);
		startingStamina = server.comment("Amount of stamina Player starts with. Values higher than maxStamina doesn't work.\n"+
				"If you want to make this value displayed as exactly one stamina wheel, you have to make this value one third of maxStamina.")
				.defineInRange("startingStamina", 1000, 0, Integer.MAX_VALUE);
		maxStaminaVessels = server.comment("Stamina Vessels players need to obtain max out stamina. More vessels means lesser stamina increase per vessel.")
				.defineInRange("maxStaminaVessels", 10, 0, Integer.MAX_VALUE);
		server.pop();

		server.push("stamina");
		paraglidingConsumesStamina = server.comment("Paragliding and ascending will consume stamina.").define("paraglidingConsumesStamina", true);
		runningConsumesStamina = server.comment("Actions other than paragliding or ascending will consume stamina.").define("runningAndSwimmingConsumesStamina", false);

		server.push("consumptions");
		for(PlayerState state : PlayerState.values()){
			state.setConfig(server.defineInRange(state.id+"StaminaConsumption", state.defaultChange, Integer.MIN_VALUE, Integer.MAX_VALUE));
		}
		server.pop();
		server.pop();

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, server.build());

		ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
		common.comment("Easy to access switches to toggle newer features on and off.\n"+
				"Most of them requires server restart or datapack reload. All of them, actually.").push("features");
		enableSpiritOrbGens = common.comment("For those who wants to remove entirety of Spirit Orbs generated from chests, more specifically...\n"+
				"  * Spirit Orbs generated in various chests\n"+
				"  * Spirit Orbs dropped by spawners and such\n"+
				"Note that bargain recipe for Heart Containers/Stamina Vessels will persist, even if this option is disabled.")
				.define("spiritOrbGens", true);
		enableHeartContainers = common.comment("For those who wants to remove entirety of Heart Containers from the game, more specifically...\n"+
				"  * Heart Containers obtained by \"challenges\" (i.e. Killing dragon, wither, raid)\n"+
				"  * Bargains using Heart Containers (custom recipes won't be affected)\n"+
				"Note that if this option is disabled while staminaVessels is enabled, \"challenges\" will drop stamina vessels instead.")
				.define("heartContainers", true);
		enableStaminaVessels = common.comment("For those who wants to remove entirety of Stamina Vessels from the game, more specifically...\n"+
				"  * Bargains using Stamina Vessels (custom recipes won't be affected)")
				.define("staminaVessels", true);
		enableStructures = common.comment("For those who wants to remove all structures added by this mod. Requires restart.")
				.define("structures", true);
		common.pop();

		common.push("debug");
		debugPlayerMovement = common.define("debugPlayerMovement", false);
		traceMovementPacket = common.define("traceMovementPacket", false);
		traceParaglidingPacket = common.define("traceParaglidingPacket", false);
		traceVesselPacket = common.define("traceVesselPacket", false);
		common.pop();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, common.build());
	}

	@SubscribeEvent
	public static void onLoad(ModConfig.Loading event){
		ModConfig cfg = event.getConfig();
		if(cfg.getModId().equals(MODID)&&cfg.getType()==ModConfig.Type.SERVER){
			windSourcesParsed = Collections.unmodifiableMap(parseWindSources());
		}
	}

	@SubscribeEvent
	public static void onReload(ModConfig.Reloading event){
		ModConfig cfg = event.getConfig();
		if(cfg.getModId().equals(MODID)&&cfg.getType()==ModConfig.Type.SERVER){
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server!=null) server.execute(() -> windSourcesParsed = Collections.unmodifiableMap(parseWindSources()));
		}
	}

	// ((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\s*#\s*([A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+(?:\s*,\s*[A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+)*))?
	private static final Pattern REGEX = Pattern.compile("^((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\\s*#\\s*([A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+(?:\\s*,\\s*[A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+)*))?$");

	private static Map<Block, Predicate<BlockState>> parseWindSources(){
		IdentityHashMap<Block, Predicate<BlockState>> map = new IdentityHashMap<>();
		Matcher m = REGEX.matcher("");
		for(String s : windSources.get()){
			if(!m.reset(s).matches()){
				warnIgnoredWindSource(s, "input pattern is incorrect");
				continue;
			}
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(m.group(1)));
			if(block==null||block==Blocks.AIR){
				warnIgnoredWindSource(s, "no such block exists");
				continue;
			}
			Predicate<BlockState> p = parsePredicate(s, m, block);
			if(p!=null) map.compute(block, (k, v) -> v==null ? p : v.or(p));
		}
		return map;
	}

	@Nullable
	private static Predicate<BlockState> parsePredicate(String input, Matcher matcher, Block block){
		String blockState = matcher.group(2);
		if(blockState==null) return s -> true;

		Map<String, String> properties = new HashMap<>();
		for(String s : blockState.split(",")){
			int i = s.indexOf('=');
			String key = s.substring(0, i);
			if(properties.containsKey(key)){
				warnIgnoredWindSource(input, "same property '{}' was defined twice", key);
				return null;
			}else properties.put(key, s.substring(i+1));
		}

		Map<Property<?>, Object> parsedProperties = new IdentityHashMap<>();
		for(Entry<String, String> e : properties.entrySet()){
			String key = e.getKey();
			Property<?> property = block.getStateContainer().getProperty(key);
			if(property==null){
				warnIgnoredWindSource(input, "property '{}' doesn't exist on that block", key);
				return null;
			}else if(parsedProperties.containsKey(property)){
				warnIgnoredWindSource(input, "same property '{}' was defined twice", key);
				return null;
			}
			Optional<?> o = property.parseValue(e.getValue());
			if(!o.isPresent()){
				warnIgnoredWindSource(input, "property '{}' doesn't contain value '{}'", key, e.getValue());
				return null;
			}
			parsedProperties.put(property, o.get());
		}
		BlockStateMatcher m = BlockStateMatcher.forBlock(block);
		for(Entry<Property<?>, Object> e : parsedProperties.entrySet()){
			Object v = e.getValue();
			m.where(e.getKey(), o -> o!=null&&o.equals(v));
		}
		return m;
	}

	private static void warnIgnoredWindSource(String input, String cause, Object... args){
		ParagliderMod.LOGGER.warn("Wind source '"+input+"' was ignored, because "+cause, args);
	}
}
