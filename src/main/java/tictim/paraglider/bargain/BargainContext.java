package tictim.paraglider.bargain;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.*;

/**
 * Pseudo-container for bargain recipes, since the system doesn't use containers. Handles syncing and such.
 *
 * @see BargainHandler#initiate(Player, Identifier, BlockPos, Identifier, Vec3)
 */
public final class BargainContext {
	private final ServerPlayer player;
	private final int sessionId;
	private final BargainType type;
	private final Identifier typeId;
	private final Map<Identifier, Bargain> bargains;

	private @Nullable Identifier advancement;
	private @Nullable Vec3 lookAt;

	private int @Nullable [] inventoryHashes;
	private int heartContainerCache;
	private int staminaVesselCache;
	private int essenceCache;

	private boolean catalogRefreshScheduled;
	private boolean finished;

	public BargainContext(@NotNull ServerPlayer player,
	                      int sessionId,
	                      @NotNull BargainType type,
	                      @NotNull Identifier typeId,
	                      @NotNull Map<@NotNull Identifier, @NotNull Bargain> bargains,
	                      @Nullable Identifier advancement,
	                      @Nullable Vec3 lookAt) {
		this.player = Objects.requireNonNull(player);
		this.sessionId = sessionId;
		this.type = Objects.requireNonNull(type);
		this.typeId = Objects.requireNonNull(typeId);
		this.bargains = Objects.requireNonNull(bargains);
		this.advancement = advancement;
		this.lookAt = lookAt;
	}

	public @NotNull ServerPlayer player() {
		return player;
	}
	public int sessionId() {
		return sessionId;
	}
	public @NotNull BargainType type() {
		return type;
	}
	public @NotNull Identifier typeId() {
		return typeId;
	}
	public @NotNull @Unmodifiable Map<@NotNull Identifier, @NotNull Bargain> bargains() {
		return Collections.unmodifiableMap(bargains);
	}
	public boolean isFinished() {
		return finished;
	}

	public @Nullable Identifier advancement() {
		return advancement;
	}
	public @Nullable Vec3 lookAt() {
		return lookAt;
	}

	public void setAdvancement(@Nullable Identifier advancement) {
		this.advancement = advancement;
	}
	public void setLookAt(@Nullable Vec3 lookAt) {
		if (Objects.equals(this.lookAt, lookAt)) return;
		this.lookAt = lookAt;
		ParagliderNetwork.get().syncBargainLookAt(this, lookAt);
	}

	public void scheduleCatalogRefresh() {
		this.catalogRefreshScheduled = true;
	}
	public void markFinished() {
		this.finished = true;
	}

	public void checkForUpdates() {
		if (isFinished()) return;

		boolean refreshCatalog = false;

		if (this.catalogRefreshScheduled) {
			refreshCatalog = true;
			this.catalogRefreshScheduled = false;
		}

		if (this.inventoryHashes == null) {
			refreshCatalog = true;
			this.inventoryHashes = new int[this.player.getInventory().getContainerSize()];
		}

		for (int i = 0; i < this.inventoryHashes.length; i++) {
			ItemStack stack = this.player.getInventory().getItem(i);
			int hash = stack.getItem().hashCode();
			hash = hash * 31 + stack.getCount();
			if (this.inventoryHashes[i] != hash) {
				refreshCatalog = true;
				this.inventoryHashes[i] = hash;
			}
		}

		VesselContainer container = VesselContainer.get(this.player);
		if (this.heartContainerCache != container.heartContainer()) {
			refreshCatalog = true;
			this.heartContainerCache = container.heartContainer();
		}
		if (this.staminaVesselCache != container.staminaVessel()) {
			refreshCatalog = true;
			this.staminaVesselCache = container.staminaVessel();
		}
		if (this.essenceCache != container.essence()) {
			refreshCatalog = true;
			this.essenceCache = container.essence();
		}

		if (refreshCatalog) {
			ParagliderNetwork.get().syncBargainCatalog(this, makeCatalog());
		}
	}

	public @NotNull List<@NotNull BargainCatalog> makeCatalog() {
		List<BargainCatalog> demands = new ArrayList<>();
		for (var e : this.bargains.entrySet()) {
			Bargain bargain = e.getValue();
			demands.add(new BargainCatalog(
					e.getKey(),
					bargain.previewDemands(),
					bargain.previewOffers(),
					new IntArrayList(bargain.countDemands(this.player)),
					bargain.bargain(this.player, true).isSuccess()));
		}
		return demands;
	}
}
