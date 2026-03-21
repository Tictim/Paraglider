package tictim.paraglider.api;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.api.stamina.StaminaEfficiencyLogicHandler;
import tictim.paraglider.api.stamina.StaminaFactory;
import tictim.paraglider.api.vessel.VesselContainer;

import java.util.Objects;
import java.util.function.Function;

import static net.minecraft.core.registries.Registries.BLOCK;
import static net.minecraft.core.registries.Registries.ITEM;

/**
 * Mod ID and a bunch of internal part of paraglider mod's API.
 */
public class ParagliderAPI {
	public static final String MODID = "paraglider";

	/**
	 * Item tag used for checking whether the item is paraglider.
	 */
	public static final TagKey<@NotNull Item> PARAGLIDERS = TagKey.create(ITEM, id("paragliders"));

	/**
	 * Block tag used for marking a block to be skipped on wind placement check, allowing wind to pass through the
	 * block.
	 */
	public static final TagKey<@NotNull Block> WIND_CAN_PASS_THROUGH = TagKey.create(BLOCK, id("wind_can_pass_through"));

	/**
	 * Group identifier used in paraglider item's UseCooldown component.
	 */
	public static final Identifier PARAGLIDER_COOLDOWN_GROUP = id("paraglider");

	public static @NotNull Identifier id(@NotNull String path) {
		return Identifier.fromNamespaceAndPath(MODID, path);
	}

	private static @Nullable Function<@NotNull Player, @NotNull Movement> movementSupplier;
	private static @Nullable Function<@NotNull Player, @NotNull Stamina> staminaSupplier;
	private static @Nullable Function<@NotNull Player, @NotNull VesselContainer> vesselContainerSupplier;
	private static @Nullable StaminaFactory staminaFactory;
	private static @Nullable ParagliderItemCapability defaultParagliderItemCapability;
	private static @Nullable StaminaEfficiencyLogicHandler staminaEfficiencyLogicHandler;

	@ApiStatus.Internal
	public static @NotNull Function<Player, Movement> movementSupplier() {
		if (movementSupplier != null) return movementSupplier;
		throw new IllegalStateException("movementSupplier is not available yet");
	}

	@ApiStatus.Internal
	public static @NotNull Function<Player, Stamina> staminaSupplier() {
		if (staminaSupplier != null) return staminaSupplier;
		throw new IllegalStateException("staminaSupplier is not available yet");
	}

	@ApiStatus.Internal
	public static @NotNull Function<Player, VesselContainer> vesselContainerSupplier() {
		if (vesselContainerSupplier != null) return vesselContainerSupplier;
		throw new IllegalStateException("vesselContainerSupplier is not available yet");
	}

	@ApiStatus.Internal
	public static @NotNull StaminaFactory staminaFactory() {
		if (staminaFactory != null) return staminaFactory;
		throw new IllegalStateException("staminaFactory is not available yet");
	}

	@ApiStatus.Internal
	public static @NotNull ParagliderItemCapability defaultParagliderItemCapability() {
		if (defaultParagliderItemCapability != null) return defaultParagliderItemCapability;
		throw new IllegalStateException("defaultParagliderItemCapability is not available yet");
	}

	@ApiStatus.Internal
	public static @NotNull StaminaEfficiencyLogicHandler staminaEfficiencyLogicHandler() {
		if (staminaEfficiencyLogicHandler != null) return staminaEfficiencyLogicHandler;
		throw new IllegalStateException("staminaEfficiencyLogicHandler is not available yet");
	}

	@ApiStatus.Internal
	public static void setMovementSupplier(@NotNull Function<Player, Movement> supplier) {
		Objects.requireNonNull(supplier);
		if (ParagliderAPI.movementSupplier == null) ParagliderAPI.movementSupplier = supplier;
		else throw new IllegalStateException("Trying to set movementSupplier twice");
	}

	@ApiStatus.Internal
	public static void setStaminaSupplier(@NotNull Function<Player, Stamina> supplier) {
		Objects.requireNonNull(supplier);
		if (ParagliderAPI.staminaSupplier == null) ParagliderAPI.staminaSupplier = supplier;
		else throw new IllegalStateException("Trying to set staminaSupplier twice");
	}

	@ApiStatus.Internal
	public static void setVesselContainerSupplier(@NotNull Function<Player, VesselContainer> supplier) {
		Objects.requireNonNull(supplier);
		if (ParagliderAPI.vesselContainerSupplier == null) ParagliderAPI.vesselContainerSupplier = supplier;
		else throw new IllegalStateException("Trying to set vesselContainerSupplier twice");
	}

	/**
	 * Do NOT call this method directly - use {@link StaminaFactory} instead.
	 *
	 * @param staminaFactory Stamina factory instance
	 */
	@ApiStatus.Internal
	public static void setStaminaFactory(@NotNull StaminaFactory staminaFactory) {
		Objects.requireNonNull(staminaFactory);
		if (ParagliderAPI.staminaFactory == null) ParagliderAPI.staminaFactory = staminaFactory;
		else throw new IllegalStateException("Trying to set staminaFactory twice");
	}

	@ApiStatus.Internal
	public static void setDefaultParagliderItemCapability(@Nullable ParagliderItemCapability c) {
		Objects.requireNonNull(c);
		if (ParagliderAPI.defaultParagliderItemCapability == null) ParagliderAPI.defaultParagliderItemCapability = c;
		else throw new IllegalStateException("Trying to set defaultParagliderItemCapability twice");
	}

	@ApiStatus.Internal
	public static void setStaminaEfficiencyLogicHandler(StaminaEfficiencyLogicHandler h) {
		Objects.requireNonNull(h);
		if (ParagliderAPI.staminaEfficiencyLogicHandler == null) ParagliderAPI.staminaEfficiencyLogicHandler = h;
		else throw new IllegalStateException("Trying to set staminaEfficiencyLogicHandler twice");
	}
}
