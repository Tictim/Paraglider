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
		if(state.staminaDelta()==0) return 0;

		List<StaminaReductionLogic> logics = logics();
		boolean hasMatch = false;
		double reductionSum = 0;
		double min = Double.NaN;
		double max = Double.NaN;

		for(StaminaReductionLogic logic : logics){
			if(!logic.isApplicable(player, state)) continue;
			hasMatch = true;
			double value = logic.getReductionRate(player, state);
			if(!Double.isNaN(value)) reductionSum += value;
			value = logic.getMinReduction(player, state);
			if(!Double.isNaN(value)) min = Double.isNaN(min) ? value : Math.min(min, value);
			value = logic.getMaxReduction(player, state);
			if(!Double.isNaN(value)) max = Double.isNaN(max) ? value : Math.max(max, value);
		}

		if(!hasMatch) return 0;
		if(!Double.isNaN(max)) reductionSum = Math.min(reductionSum, max);
		if(!Double.isNaN(min)) reductionSum = Math.max(reductionSum, min);
		return reductionSum;
	}
}
