package tictim.paraglider.impl.vessel;

import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.vessel.VesselContainer;

/**
 * Implementation of {@link VesselContainer} that rejects all forms of transactions.
 */
public final class NullVesselContainer implements VesselContainer{
	private static final NullVesselContainer instance = new NullVesselContainer();

	@NotNull public static NullVesselContainer get(){
		return instance;
	}

	@Override public int heartContainer(){
		return 0;
	}
	@Override public int staminaVessel(){
		return 0;
	}
	@Override public int essence(){
		return 0;
	}

	@Override @NotNull public SetResult setHeartContainer(int amount, boolean simulate, boolean playEffect){
		return SetResult.FAIL;
	}
	@Override @NotNull public SetResult setStaminaVessel(int amount, boolean simulate, boolean playEffect){
		return SetResult.FAIL;
	}
	@Override @NotNull public SetResult setEssence(int amount, boolean simulate, boolean playEffect){
		return SetResult.FAIL;
	}

	@Override public int giveHeartContainers(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}
	@Override public int giveStaminaVessels(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}
	@Override public int giveEssences(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}

	@Override public int takeHeartContainers(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}
	@Override public int takeStaminaVessels(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}
	@Override public int takeEssences(int quantity, boolean simulate, boolean playEffect){
		return 0;
	}

	@Override public String toString(){
		return "NullVesselContainer";
	}
}
