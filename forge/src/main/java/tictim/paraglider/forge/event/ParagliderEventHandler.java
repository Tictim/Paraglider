package tictim.paraglider.forge.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.bargain.BargainHandler;
import tictim.paraglider.forge.capability.PlayerMovementProvider;
import tictim.paraglider.impl.movement.ClientPlayerMovement;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.RemotePlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;
import tictim.paraglider.network.ParagliderNetwork;

import static tictim.paraglider.api.ParagliderAPI.MODID;
import static tictim.paraglider.api.movement.ParagliderPlayerStates.Flags.FLAG_PARAGLIDING;

@Mod.EventBusSubscriber(modid = MODID)
public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelable()&&event.getHand()==InteractionHand.OFF_HAND){
			Movement movement = Movement.get(event.getEntity());
			if(movement.state().has(FLAG_PARAGLIDING)) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerStartUseItem(LivingEntityUseItemEvent.Start event){
		if(!(event.getEntity() instanceof Player player)) return;
		Movement movement = Movement.get(player);
		if(movement.state().has(FLAG_PARAGLIDING)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPlayerTickUseItem(LivingEntityUseItemEvent.Tick event){
		if(!(event.getEntity() instanceof Player player)) return;
		Movement movement = Movement.get(player);
		if(movement.state().has(FLAG_PARAGLIDING)) player.stopUsingItem();
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event){
		Player original = event.getOriginal();
		original.reviveCaps();
		PlayerMovement m1 = PlayerMovementProvider.of(original);
		if(m1!=null){
			PlayerMovement m2 = PlayerMovementProvider.of(event.getEntity());
			if(m2!=null){
				m2.copyFrom(m1);
				if(event.isWasDeath()){
					m2.stamina().setStamina(m2.stamina().maxStamina());
				}
			}
		}
		original.invalidateCaps();
	}

	private static final ResourceLocation MOVEMENT_HANDLER_KEY = ParagliderAPI.id("paragliding_movement_handler");

	@SubscribeEvent
	public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event){
		if(!(event.getObject() instanceof Player player)) return;
		event.addCapability(MOVEMENT_HANDLER_KEY, PlayerMovementProvider.create(
				player instanceof ServerPlayer serverPlayer ?
						new ServerPlayerMovement(serverPlayer) :
						DistExecutor.unsafeRunForDist(
								() -> () -> Client.createPlayerMovement(player),
								() -> () -> new RemotePlayerMovement(player))));
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		if(Movement.get(event.player) instanceof PlayerMovement playerMovement)
			playerMovement.update();
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event){
		if(!(event.getTarget() instanceof Player tracking)||!(event.getEntity() instanceof ServerPlayer player)) return;
		ParagliderNetwork.get().syncRemoteMovement(tracking, player, Movement.get(tracking).state().id());
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event){
		if(event.phase==TickEvent.Phase.END) BargainHandler.update();
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
		if(event.getEntity() instanceof ServerPlayer player)
			ParagliderNetwork.get().syncStateMap(player, ParagliderMod.instance().getLocalPlayerStateMap());
	}

	private static final class Client{
		@NotNull public static PlayerMovement createPlayerMovement(@NotNull Player player){
			return player instanceof LocalPlayer localPlayer ?
					new ClientPlayerMovement(localPlayer) :
					new RemotePlayerMovement(player);
		}
	}
}
