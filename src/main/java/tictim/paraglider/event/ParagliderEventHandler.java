package tictim.paraglider.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.contents.Contents;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;
import tictim.paraglider.network.ParagliderNetwork;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

@EventBusSubscriber(modid = MODID)
public final class ParagliderEventHandler {
	private ParagliderEventHandler() {}

	static {
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteractSpecific event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickEmpty event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> onPlayerInteract(event));
		NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickEmpty event) -> onPlayerInteract(event));
	}

	public static void onPlayerInteract(PlayerInteractEvent event) {
		if (!(event instanceof ICancellableEvent cancellable)) return;

		if (event.getHand() == InteractionHand.OFF_HAND) {
			Movement movement = Movement.get(event.getEntity());
			if (movement.state().has(FLAG_PARAGLIDING)) cancellable.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerStartUseItem(LivingEntityUseItemEvent.Start event) {
		if (!(event.getEntity() instanceof Player player)) return;
		Movement movement = Movement.get(player);
		if (movement.state().has(FLAG_PARAGLIDING)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPlayerTickUseItem(LivingEntityUseItemEvent.Tick event) {
		if (!(event.getEntity() instanceof Player player)) return;
		Movement movement = Movement.get(player);
		if (movement.state().has(FLAG_PARAGLIDING)) player.stopUsingItem();
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (!event.isWasDeath()) return;

		PlayerMovement prev = event.getOriginal().getExistingDataOrNull(Contents.get().playerMovement());
		if (prev == null) return;

		PlayerMovement current = event.getEntity().getData(Contents.get().playerMovement());

		current.stamina().setStamina(current.stamina().maxStamina());
	}

	@SubscribeEvent
	public static void afterPlayerTick(PlayerTickEvent.Post event) {
		if (Movement.get(event.getEntity()) instanceof PlayerMovement playerMovement)
			playerMovement.update();
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (!(event.getTarget() instanceof Player tracking) || !(event.getEntity() instanceof ServerPlayer player))
			return;
		ParagliderNetwork.get().syncRemoteMovement(tracking, player, Movement.get(tracking).state().id());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (Movement.get(event.getEntity()) instanceof ServerPlayerMovement serverPlayerMovement) {
			serverPlayerMovement.markForSync();
		}
	}

	@SubscribeEvent
	public static void afterServerTick(ServerTickEvent.Post event) {
		BargainHandler.update();
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ParagliderNetwork.get().syncStateMap(player, ParagliderMod.instance().getLocalPlayerStateMap());
			if (Movement.get(player) instanceof ServerPlayerMovement serverPlayerMovement) {
				serverPlayerMovement.markForSync();
			}
		}
	}
}
