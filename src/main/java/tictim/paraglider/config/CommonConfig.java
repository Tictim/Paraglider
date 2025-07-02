package tictim.paraglider.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig implements FeatureCfg, DebugCfg {
	public final ModConfigSpec spec;

	private final ModConfigSpec.BooleanValue enableSpiritOrbGens;
	private final ModConfigSpec.BooleanValue enableHeartContainers;
	private final ModConfigSpec.BooleanValue enableStaminaVessels;
	private final ModConfigSpec.BooleanValue enableVillageStructures;

	private final ModConfigSpec.BooleanValue debugPlayerMovement;
	private final ModConfigSpec.BooleanValue traceMovementPacket;
	private final ModConfigSpec.BooleanValue traceVesselPacket;
	private final ModConfigSpec.BooleanValue traceBargainPacket;
	private final ModConfigSpec.BooleanValue traceWindPacket;

	private final ModConfigSpec.BooleanValue verboseWindSourceLoading;

	public CommonConfig() {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();
		b.comment("""
						Easy to access switches to toggle side features on and off.
						Most of them requires server restart or datapack reload. All of them, actually.""")
				.push("features");
		enableSpiritOrbGens = b.comment("""
						For those who wants to remove Spirit Orbs generated in the world, more specifically...
						  * Spirit Orbs generated in various chests
						  * Spirit Orbs dropped by spawners and such
						Note that bargain recipe for Heart Containers/Stamina Vessels will persist, even if this option is disabled.""")
				.define("spiritOrbGens", true);
		enableHeartContainers = b.comment("""
						For those who wants to remove entirety of Heart Containers from the game, more specifically...
						  * Heart Containers obtained by "challenges" (i.e. Killing dragon, wither, raid)
						  * Bargains using Heart Containers (custom recipes won't be affected)
						Note that if this option is disabled while staminaVessels is enabled, "challenges" will drop Stamina Vessels instead.""")
				.define("heartContainers", true);
		enableStaminaVessels = b.comment("""
						For those who wants to remove entirety of Stamina Vessels from the game, more specifically...
						  * Bargains using Stamina Vessels (custom recipes won't be affected)""")
				.define("staminaVessels", true);
		enableVillageStructures = b.comment("""
						For those who wants to remove village structures added by this mod. Requires datapack reload.
						Note that the structures generated in other places are NOT disabled by this option, and requires a datapack to remove.""")
				.define("villageStructures", true);
		b.pop();

		b.push("debug");
		debugPlayerMovement = b.define("debugPlayerMovement", false);
		traceMovementPacket = b.define("traceMovementPacket", false);
		traceVesselPacket = b.define("traceVesselPacket", false);
		traceBargainPacket = b.define("traceBargainPacket", false);
		traceWindPacket = b.define("traceWindPacket", false);
		verboseWindSourceLoading = b.define("verboseWindSourceLoading", false);
		b.pop();

		spec = b.build();
	}

	@Override public boolean enableSpiritOrbGens() {
		return enableSpiritOrbGens.get();
	}
	@Override public boolean enableHeartContainers() {
		return enableHeartContainers.get();
	}
	@Override public boolean enableStaminaVessels() {
		return enableStaminaVessels.get();
	}
	@Override public boolean enableVillageStructures() {
		return enableVillageStructures.get();
	}

	@Override public boolean debugPlayerMovement() {
		return debugPlayerMovement.get();
	}
	@Override public boolean traceMovementPacket() {
		return traceMovementPacket.get();
	}
	@Override public boolean traceVesselPacket() {
		return traceVesselPacket.get();
	}
	@Override public boolean traceBargainPacket() {
		return traceBargainPacket.get();
	}
	@Override public boolean traceWindPacket() {
		return traceWindPacket.get();
	}

	@Override public boolean verboseWindSourceLoading() {
		return verboseWindSourceLoading.get();
	}
}
