package tictim.paraglider;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class ModCfg{
	private ModCfg(){}

	private static BooleanValue ascendingWinds;
	private static BooleanValue paraglidingConsumesStamina;
	private static BooleanValue runningConsumesStamina;

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
}
