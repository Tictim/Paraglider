package tictim.paraglider.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig implements FeatureCfg, DebugCfg{
	protected final ForgeConfigSpec spec;

	private final ForgeConfigSpec.BooleanValue enableSpiritOrbGens;
	private final ForgeConfigSpec.BooleanValue enableHeartContainers;
	private final ForgeConfigSpec.BooleanValue enableStaminaVessels;
	private final ForgeConfigSpec.BooleanValue enableStructures;

	private final ForgeConfigSpec.BooleanValue debugPlayerMovement;
	private final ForgeConfigSpec.BooleanValue traceMovementPacket;
	private final ForgeConfigSpec.BooleanValue traceVesselPacket;
	private final ForgeConfigSpec.BooleanValue traceBargainPacket;
	private final ForgeConfigSpec.BooleanValue traceWindPacket;

	public CommonConfig(){
		ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
		common.comment("""
						Easy to access switches to toggle side features on and off.
						Most of them requires server restart or datapack reload. All of them, actually.""")
				.push("features");
		enableSpiritOrbGens = common.comment("""
						For those who wants to remove Spirit Orbs generated in the world, more specifically...
						  * Spirit Orbs generated in various chests
						  * Spirit Orbs dropped by spawners and such
						Note that bargain recipe for Heart Containers/Stamina Vessels will persist, even if this option is disabled.""")
				.define("spiritOrbGens", true);
		enableHeartContainers = common.comment("""
						For those who wants to remove entirety of Heart Containers from the game, more specifically...
						  * Heart Containers obtained by "challenges" (i.e. Killing dragon, wither, raid)
						  * Bargains using Heart Containers (custom recipes won't be affected)
						Note that if this option is disabled while staminaVessels is enabled, "challenges" will drop stamina vessels instead.""")
				.define("heartContainers", true);
		enableStaminaVessels = common.comment("""
						For those who wants to remove entirety of Stamina Vessels from the game, more specifically...
						  * Bargains using Stamina Vessels (custom recipes won't be affected)""")
				.define("staminaVessels", true);
		enableStructures = common.comment("For those who wants to remove all structures added by this mod. Requires restart.")
				.define("structures", true);
		common.pop();

		common.push("debug");
		debugPlayerMovement = common.define("debugPlayerMovement", false);
		traceMovementPacket = common.define("traceMovementPacket", false);
		traceVesselPacket = common.define("traceVesselPacket", false);
		traceBargainPacket = common.define("traceBargainPacket", false);
		traceWindPacket = common.define("traceWindPacket", false);
		common.pop();

		spec = common.build();
	}

	@Override public boolean enableSpiritOrbGens(){
		return enableSpiritOrbGens.get();
	}
	@Override public boolean enableHeartContainers(){
		return enableHeartContainers.get();
	}
	@Override public boolean enableStaminaVessels(){
		return enableStaminaVessels.get();
	}
	@Override public boolean enableStructures(){
		return enableStructures.get();
	}

	@Override public boolean debugPlayerMovement(){
		return debugPlayerMovement.get();
	}
	@Override public boolean traceMovementPacket(){
		return traceMovementPacket.get();
	}
	@Override public boolean traceVesselPacket(){
		return traceVesselPacket.get();
	}
	@Override public boolean traceBargainPacket(){
		return traceBargainPacket.get();
	}
	@Override public boolean traceWindPacket(){
		return traceWindPacket.get();
	}
}
