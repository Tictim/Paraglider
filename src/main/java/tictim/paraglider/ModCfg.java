package tictim.paraglider;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
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

	private static BooleanValue debugPlayerMovement;
	private static BooleanValue traceMovementPacket;
	private static BooleanValue traceParaglidingPacket;
	private static BooleanValue traceVesselPacket;

	private static BooleanValue forceFlightDisabled;

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

	public static boolean forceFlightDisabled(){
		return forceFlightDisabled.get();
	}

	public static void init(){
		ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
		ascendingWinds = server.comment("Fire will float you upward.").define("ascendingWinds", true);
		windSources = server.comment("You can customize which block produces wind.\n"+
				"Write each blockstate to one of this format:\n"+
				"  [block ID]   (Matches all state of the block)\n"+
				"  [block ID]#[property1=value],[property2=value],[property3=value]   (Matches state of the block that has specified properties)\n" +
				"Same property cannot be specified multiple times. Wind sources with any invalid part will be excluded.")
				.defineList("windSources",
						ImmutableList.of("fire",
								"campfire#lit=true",
								"soul_campfire#lit=true"),
						o -> MATCH.reset(o.toString()).matches());
		paraglidingConsumesStamina = server.comment("Paragliding will consume stamina.").define("paraglidingConsumesStamina", true);
		runningConsumesStamina = server.comment("Actions other than paragliding will consume stamina.").define("runningAndSwimmingConsumesStamina", false);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, server.build());

		ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
		common.push("debug");
		debugPlayerMovement = common.define("debugPlayerMovement", false);
		traceMovementPacket = common.define("traceMovementPacket", false);
		traceParaglidingPacket = common.define("traceParaglidingPacket", false);
		traceVesselPacket = common.define("traceVesselPacket", false);
		common.pop();
		forceFlightDisabled = common.worldRestart().comment("Forces the server to not kick the shit out of 'cheaters' who also happened to be using paraglider.\n"+
				"Feel free to disable it if you hate 'cheaters'. Or paraglider. If disabled, 'allow-flight' inside server.properties will be used.").define("forceFlightDisabled", true);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, common.build());
	}


	@SubscribeEvent
	public static void onLoad(ModConfig.ModConfigEvent event){
		ModConfig cfg = event.getConfig();
		if(cfg.getModId().equals(MODID)&&cfg.getType()==ModConfig.Type.SERVER){
			windSourcesParsed = Collections.unmodifiableMap(parseWindSources());
		}
	}

	// ((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\s*#\s*([A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+(?:\s*,\s*[A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+)*))?
	private static final Pattern REGEX = Pattern.compile("^((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\\s*#\\s*([A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+(?:\\s*,\\s*[A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+)*))?$");
	private static final Matcher MATCH = REGEX.matcher("");

	private static Map<Block, Predicate<BlockState>> parseWindSources(){
		IdentityHashMap<Block, Predicate<BlockState>> map = new IdentityHashMap<>();
		for(String s : windSources.get()){
			if(MATCH.reset(s).matches()){
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MATCH.group(1)));
				if(block!=null){
					Predicate<BlockState> p = parsePredicate(block);
					if(p!=null) map.compute(block, (k, v) -> v==null ? p : v.or(p));
				}
			}
		}
		return map;
	}

	@Nullable
	private static Predicate<BlockState> parsePredicate(Block block){
		String match = MATCH.group(2);
		if(match==null) return s -> true;

		Map<String, String> properties = new HashMap<>();
		for(String s : match.split(",")){
			int i = s.indexOf('=');
			String key = s.substring(0, i);
			if(properties.containsKey(key)) return null;
			else properties.put(key, s.substring(i+1));
		}

		Map<Property<?>, Object> parsedProperties = new IdentityHashMap<>();
		for(Entry<String, String> e : properties.entrySet()){
			String key = e.getKey();
			Property<?> property = block.getStateContainer().getProperty(key);
			if(property==null||parsedProperties.containsKey(property)) return null;
			Optional<?> o = property.parseValue(e.getValue());
			if(!o.isPresent()) return null;
			parsedProperties.put(property, o.get());
		}
		BlockStateMatcher m = BlockStateMatcher.forBlock(block);
		for(Entry<Property<?>, Object> e : parsedProperties.entrySet()){
			Object v = e.getValue();
			m.where(e.getKey(), o -> o!=null&&o.equals(v));
		}
		return m;
	}
}
