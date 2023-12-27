package tictim.paraglider.bargain;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.BargainType;
import tictim.paraglider.contents.BargainTypeRegistry;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static tictim.paraglider.ParagliderUtils.DIALOG_RNG;

/**
 * Static class for handling {@link BargainContext} objects.
 */
public final class BargainHandler{
	private BargainHandler(){}

	private static final Map<ServerPlayer, BargainContext> bargains = new Object2ObjectOpenHashMap<>();
	private static int bargainSessionIncr;

	/**
	 * Try to initiate a bargain with given player and context. If another bargain was in place with the player before,
	 * the previous one will finish and new one will take place.<br/>
	 * This method can fail to initiate bargain, if:
	 * <ul>
	 * <li>player is not an instance of {@link ServerPlayer},</li>
	 * <li>no bargain type is associated with ID {@code bargainType},</li>
	 * <li>or no bargains are available for the player, in provided context.</li>
	 * </ul>
	 *
	 * @param player      Player
	 * @param bargainType ID of the bargain type
	 * @param pos         Optional block position
	 * @param advancement Optional ID of the advancement. After successful bargain, if the value exists, criterion with
	 *                    name {@code bargain} will be triggered for the advancement specified.
	 * @param lookAt      Optional position to orient player's head to
	 * @return Whether the bargain was successfully created or not
	 * @throws NullPointerException If {@code player == null || bargainType == null}
	 */
	public static boolean initiate(@NotNull Player player,
	                               @NotNull ResourceLocation bargainType,
	                               @Nullable BlockPos pos,
	                               @Nullable ResourceLocation advancement,
	                               @Nullable Vec3 lookAt){
		Objects.requireNonNull(player, "player == null");
		if(!(player instanceof ServerPlayer serverPlayer)) return false;

		BargainType type = BargainTypeRegistry.get().getFromID(serverPlayer.serverLevel(),
				Objects.requireNonNull(bargainType, "bargainType == null"));
		if(type==null) return false;

		var bargains = player.level().getRecipeManager()
				.getAllRecipesFor(Contents.get().bargainRecipeType())
				.stream()
				.filter(holder -> bargainType.equals(holder.value().getBargainType())&&holder.value().isAvailableFor(player, pos))
				.collect(Collectors.toCollection(ObjectOpenHashSet::new));

		if(bargains.isEmpty()) return false;

		BargainContext ctx = new BargainContext(serverPlayer, bargainSessionIncr++, type, bargainType, bargains, advancement, lookAt);

		BargainContext prevCtx = BargainHandler.bargains.put(serverPlayer, ctx);
		if(prevCtx!=null) prevCtx.markFinished(); // mark previous bargain as finished, if it exists

		ParagliderNetwork.get().initBargain(ctx, type.dialog().randomInitialDialog(DIALOG_RNG));
		return true;
	}

	/**
	 * @return Unmodifiable view of ongoing bargains
	 */
	@NotNull @Unmodifiable public static Map<@NotNull ServerPlayer, @NotNull BargainContext> bargains(){
		return Collections.unmodifiableMap(bargains);
	}

	/**
	 * Get {@link BargainContext} instance of the player's ongoing bargain.
	 *
	 * @param player Player
	 * @return {@link BargainContext} instance of the player's ongoing bargain, or {@code null} if no such bargain exists.
	 */
	@Nullable public static BargainContext getBargain(@NotNull ServerPlayer player){
		return bargains.get(player);
	}

	@ApiStatus.Internal
	public static void update(){
		for(var it = bargains.values().iterator(); it.hasNext(); ){
			BargainContext ctx = it.next();
			if(ctx.isFinished()){
				it.remove();
				ParagliderNetwork.get().bargainEndToClient(ctx);
				continue;
			}
			ctx.checkForUpdates();
			if(ctx.isFinished()){
				it.remove();
				ParagliderNetwork.get().bargainEndToClient(ctx);
			}
		}
	}
}
