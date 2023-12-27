package tictim.paraglider.bargain;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.api.vessel.VesselContainer;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pseudo-container for bargain recipes, since the system doesn't use containers. Handles syncing and such.
 *
 * @see BargainHandler#initiate(Player, ResourceLocation, BlockPos, ResourceLocation, Vec3)
 */
public final class BargainContext{
	private final ServerPlayer player;
	private final int sessionId;
	private final BargainType type;
	private final ResourceLocation typeId;
	private final Set<RecipeHolder<Bargain>> bargains;

	@Nullable private ResourceLocation advancement;
	@Nullable private Vec3 lookAt;

	private int @Nullable [] inventoryHashes;
	private int heartContainerCache;
	private int staminaVesselCache;
	private int essenceCache;

	private boolean catalogRefreshScheduled;
	private boolean finished;

	public BargainContext(@NotNull ServerPlayer player,
	                      int sessionId,
	                      @NotNull BargainType type,
	                      @NotNull ResourceLocation typeId,
	                      @NotNull Set<RecipeHolder<Bargain>> bargains,
	                      @Nullable ResourceLocation advancement,
	                      @Nullable Vec3 lookAt){
		this.player = Objects.requireNonNull(player);
		this.sessionId = sessionId;
		this.type = Objects.requireNonNull(type);
		this.typeId = Objects.requireNonNull(typeId);
		this.bargains = Objects.requireNonNull(bargains);
		this.advancement = advancement;
		this.lookAt = lookAt;
	}

	@NotNull public ServerPlayer player(){
		return player;
	}
	public int sessionId(){
		return sessionId;
	}
	@NotNull public BargainType type(){
		return type;
	}
	@NotNull public ResourceLocation typeId(){
		return typeId;
	}
	@NotNull @Unmodifiable public Map<@NotNull ResourceLocation, @NotNull RecipeHolder<@NotNull Bargain>> bargains(){
		return bargains.stream().collect(Collectors.toUnmodifiableMap(RecipeHolder::id, holder -> holder, (holder1, holder2) -> holder1));
	}
	public boolean isFinished(){
		return finished;
	}

	@Nullable public ResourceLocation advancement(){
		return advancement;
	}
	@Nullable public Vec3 lookAt(){
		return lookAt;
	}

	public void setAdvancement(@Nullable ResourceLocation advancement){
		this.advancement = advancement;
	}
	public void setLookAt(@Nullable Vec3 lookAt){
		if(Objects.equals(this.lookAt, lookAt)) return;
		this.lookAt = lookAt;
		ParagliderNetwork.get().syncBargainLookAt(this, lookAt);
	}

	public void scheduleCatalogRefresh(){
		this.catalogRefreshScheduled = true;
	}
	public void markFinished(){
		this.finished = true;
	}

	public void checkForUpdates(){
		if(isFinished()) return;

		boolean refreshCatalog = false;
		if(this.catalogRefreshScheduled){
			refreshCatalog = true;
			this.catalogRefreshScheduled = false;
		}
		if(this.inventoryHashes==null){
			refreshCatalog = true;
			this.inventoryHashes = new int[this.player.getInventory().getContainerSize()];
		}
		for(int i = 0; i<this.inventoryHashes.length; i++){
			ItemStack stack = this.player.getInventory().getItem(i);
			int hash = stack.getItem().hashCode();
			hash = hash*31+stack.getCount();
			hash = hash*31+Objects.hashCode(stack.getTag());
			if(this.inventoryHashes[i]!=hash){
				refreshCatalog = true;
				this.inventoryHashes[i] = hash;
			}
		}

		VesselContainer container = VesselContainer.get(this.player);
		if(this.heartContainerCache!=container.heartContainer()){
			refreshCatalog = true;
			this.heartContainerCache = container.heartContainer();
		}
		if(this.staminaVesselCache!=container.staminaVessel()){
			refreshCatalog = true;
			this.staminaVesselCache = container.staminaVessel();
		}
		if(this.essenceCache!=container.essence()){
			refreshCatalog = true;
			this.essenceCache = container.essence();
		}
		if(refreshCatalog){
			ParagliderNetwork.get().syncBargainCatalog(this, makeCatalog());
		}
	}

	@NotNull public Map<@NotNull ResourceLocation, @NotNull BargainCatalog> makeCatalog(){
		Map<ResourceLocation, BargainCatalog> demands = new Object2ObjectOpenHashMap<>();
		for(RecipeHolder<Bargain> holder : this.bargains){
			Bargain bargain = holder.value();
			demands.put(holder.id(), new BargainCatalog(
					holder.id(),
					bargain.previewDemands().stream()
							.mapToInt(demandPreview -> demandPreview.count(this.player))
							.toArray(),
					bargain.bargain(this.player, true).isSuccess()));
		}
		return demands;
	}
}
