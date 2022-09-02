package tictim.paraglider.event;

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
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.ClientPlayerMovement;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncParaglidingMsg;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = ParagliderMod.MODID)
public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	// PlayerEntity#livingTick(), default value of jumpMovementFactor while sprinting
	private static final double DEFAULT_PARAGLIDING_SPEED = 0.02+0.005999999865889549;

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelable()){
			// use PlayerMovement instead of ServerPlayerMovement; this also needs to trigger client side to avoid desync
			PlayerMovement m = PlayerMovement.of(event.getPlayer());
			if(m!=null&&m.isParagliding()) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event){
		Player original = event.getOriginal();
		original.reviveCaps();
		PlayerMovement m1 = PlayerMovement.of(original);
		PlayerMovement m2 = PlayerMovement.of(event.getPlayer());
		if(m1!=null&&m2!=null) m1.copyTo(m2);
		original.invalidateCaps();
	}

	@SubscribeEvent
	public static void onPlayerStartUseItem(LivingEntityUseItemEvent.Start event){
		if(event.getEntityLiving() instanceof Player){
			// use PlayerMovement instead of ServerPlayerMovement; this also needs to trigger client side to avoid desync
			PlayerMovement m = PlayerMovement.of(event.getEntityLiving());
			if(m!=null&&m.isParagliding()) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerTickUseItem(LivingEntityUseItemEvent.Tick event){
		if(event.getEntityLiving() instanceof Player){
			// use PlayerMovement instead of ServerPlayerMovement; this also needs to trigger client side to avoid desync
			PlayerMovement m = PlayerMovement.of(event.getEntityLiving());
			if(m!=null&&m.isParagliding()) event.getEntityLiving().stopUsingItem();
		}
	}

	private static final ResourceLocation MOVEMENT_HANDLER_KEY = new ResourceLocation(MODID, "paragliding_movement_handler");

	@SubscribeEvent
	public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event){
		if(event.getObject() instanceof Player p){
			PlayerMovement m = p instanceof ServerPlayer ? new ServerPlayerMovement((ServerPlayer)p) :
					DistExecutor.unsafeRunForDist(
							() -> () -> Client.createPlayerMovement(p),
							() -> () -> new RemotePlayerMovement(p));

			event.addCapability(MOVEMENT_HANDLER_KEY, m);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		PlayerMovement h = PlayerMovement.of(event.player);
		if(h!=null){
			if(event.phase==TickEvent.Phase.END){
				h.update();
			}else{
				if(h.isParagliding()){
					double v = ModCfg.paraglidingSpeed();
					event.player.flyingSpeed = (float)(DEFAULT_PARAGLIDING_SPEED*v);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		ServerPlayerMovement h = ServerPlayerMovement.of(event.getPlayer());
		if(h!=null){
			h.movementNeedsSync = true;
			h.paraglidingNeedsSync = true;
			h.vesselNeedsSync = true;
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event){
		Player player = event.getPlayer();
		if(player instanceof ServerPlayer sp){
			PlayerMovement h = PlayerMovement.of(event.getTarget());
			if(h!=null){
				SyncParaglidingMsg msg = new SyncParaglidingMsg(h);
				if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Sending packet {} from player {} to player {}", msg, h.player, player);
				ModNet.NET.send(PacketDistributor.PLAYER.with(() -> sp), msg);
			}
		}
	}

	private static final class Client{
		public static PlayerMovement createPlayerMovement(Player player){
			return player instanceof LocalPlayer ? new ClientPlayerMovement(player) : new RemotePlayerMovement(player);
		}
	}
}
