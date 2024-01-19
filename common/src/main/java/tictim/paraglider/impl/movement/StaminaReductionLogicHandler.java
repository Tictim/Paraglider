package tictim.paraglider.impl.movement;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.movement.StaminaReductionLogic;

import java.util.List;

public final class StaminaReductionLogicHandler{
	private StaminaReductionLogicHandler(){}

	private static boolean initialized;

	private static List<StaminaReductionLogic> logics;

	public static void init(){
		if(initialized) return;
		initialized = true;
		logics = StaminaReductionLogicLoader.loadStaminaReductionLogics();
	}

	@NotNull @Unmodifiable public static List<StaminaReductionLogic> logics(){
		if(logics==null) throw new IllegalStateException("Stamina reduction logic is not ready yet");
		return logics;
	}

	public static double getReductionRate(@NotNull Player player, @NotNull PlayerState state){
		if(state.staminaDelta() == 0) return 0;

		List<StaminaReductionLogic> logics = logics();
		boolean hasMatch = false;
		double reductionSum = 0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for(StaminaReductionLogic logic : logics){
			if(!logic.isApplicable(player, state)) continue;
			hasMatch = true;
			reductionSum += logic.getReductionRate(player, state);
			min = Math.min(min, logic.getMinReduction(player, state));
			max = Math.max(max, logic.getMaxReduction(player, state));
		}

		return hasMatch ? Math.min(Math.max(reductionSum, max), min) : 0;
	}
}
