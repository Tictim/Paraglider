package tictim.paraglider.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaFactory;
import tictim.paraglider.api.vessel.VesselContainer;

import java.util.Objects;
import java.util.function.Function;

/**
 * Internal part of paraglider mod's API. Not recommended for use.
 */
public class ParagliderAPI{
	public static final String MODID = "paraglider";

	@NotNull public static ResourceLocation id(String path){
		return new ResourceLocation(MODID, path);
	}

	private static Function<@NotNull Player, @NotNull Movement> movementSupplier;
	private static Function<@NotNull Player, @NotNull Stamina> staminaSupplier;
	private static Function<@NotNull Player, @NotNull VesselContainer> vesselContainerSupplier;
	private static StaminaFactory staminaFactory;

	@ApiStatus.Internal
	@NotNull public static Function<Player, Movement> movementSupplier(){
		if(movementSupplier!=null) return movementSupplier;
		throw new IllegalStateException("movementSupplier is not available yet");
	}

	@ApiStatus.Internal
	@NotNull public static Function<Player, Stamina> staminaSupplier(){
		if(staminaSupplier!=null) return staminaSupplier;
		throw new IllegalStateException("staminaSupplier is not available yet");
	}

	@ApiStatus.Internal
	@NotNull public static Function<Player, VesselContainer> vesselContainerSupplier(){
		if(vesselContainerSupplier!=null) return vesselContainerSupplier;
		throw new IllegalStateException("vesselContainerSupplier is not available yet");
	}

	@ApiStatus.Internal
	@NotNull public static StaminaFactory staminaFactory(){
		if(staminaFactory!=null) return staminaFactory;
		throw new IllegalStateException("staminaFactory is not available yet");
	}

	@ApiStatus.Internal
	public static void setMovementSupplier(@NotNull Function<Player, Movement> supplier){
		Objects.requireNonNull(supplier);
		if(ParagliderAPI.movementSupplier==null) ParagliderAPI.movementSupplier = supplier;
		else throw new IllegalStateException("Trying to set movementSupplier twice");
	}

	@ApiStatus.Internal
	public static void setStaminaSupplier(@NotNull Function<Player, Stamina> supplier){
		Objects.requireNonNull(supplier);
		if(ParagliderAPI.staminaSupplier==null) ParagliderAPI.staminaSupplier = supplier;
		else throw new IllegalStateException("Trying to set staminaSupplier twice");
	}

	@ApiStatus.Internal
	public static void setVesselContainerSupplier(@NotNull Function<Player, VesselContainer> supplier){
		Objects.requireNonNull(supplier);
		if(ParagliderAPI.vesselContainerSupplier==null) ParagliderAPI.vesselContainerSupplier = supplier;
		else throw new IllegalStateException("Trying to set vesselContainerSupplier twice");
	}

	/**
	 * Do NOT call this method directly - use {@link StaminaFactory} instead.
	 *
	 * @param staminaFactory Stamina factory instance
	 */
	@ApiStatus.Internal
	public static void setStaminaFactory(@NotNull StaminaFactory staminaFactory){
		Objects.requireNonNull(staminaFactory);
		if(ParagliderAPI.staminaFactory==null) ParagliderAPI.staminaFactory = staminaFactory;
		else throw new IllegalStateException("Trying to set staminaFactory twice");
	}
}
