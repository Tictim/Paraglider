package tictim.paraglider.event;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.ClientPlayerMovement;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.item.ParagliderItem;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncParaglidingMsg;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = ParagliderMod.MODID)
public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	// PlayerEntity#livingTick(), default value of jumpMovementFactor while sprinting
	private static final double DEFAULT_PARAGLIDING_SPEED = 0.02+0.005999999865889549;

	@SubscribeEvent
	public static void serverSetup(FMLServerStartedEvent event){
		if(ModCfg.forceFlightDisabled()) event.getServer().setAllowFlight(true);
	}

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelable()&&event.getHand()==Hand.OFF_HAND){
			if(ParagliderItem.hasParaglidingFlag(event.getPlayer().getHeldItemMainhand())) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event){
		PlayerMovement m1 = event.getOriginal().getCapability(PlayerMovement.CAP).orElse(null);
		PlayerMovement m2 = event.getPlayer().getCapability(PlayerMovement.CAP).orElse(null);
		if(m1!=null&&m2!=null){
			m2.setRecoveryDelay(m1.getRecoveryDelay());
			m2.setStaminaVessels(m1.getStaminaVessels());
			m2.setHeartContainers(m1.getHeartContainers());
			m2.setStamina(m2.getMaxStamina());
		}
	}

	@SubscribeEvent
	public static void onPlayerUseItem(LivingEntityUseItemEvent.Tick event){
		if(event.getEntityLiving().getActiveHand()==Hand.OFF_HAND&&event.getEntityLiving() instanceof PlayerEntity){
			if(ParagliderItem.hasParaglidingFlag(event.getEntityLiving().getHeldItemMainhand())) event.getEntityLiving().resetActiveHand();
		}
	}

	private static final ResourceLocation MOVEMENT_HANDLER_KEY = new ResourceLocation(MODID, "paragliding_movement_handler");

	@SubscribeEvent
	public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event){
		Entity o = event.getObject();
		if(o instanceof PlayerEntity){
			PlayerEntity p = (PlayerEntity)o;
			PlayerMovement m = p instanceof ServerPlayerEntity ? new ServerPlayerMovement((ServerPlayerEntity)p) :
					DistExecutor.unsafeRunForDist(
							() -> () -> Client.createPlayerMovement(p),
							() -> () -> new RemotePlayerMovement(p));

			event.addCapability(MOVEMENT_HANDLER_KEY, m);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		PlayerMovement h = event.player.getCapability(PlayerMovement.CAP).orElse(null);
		if(h!=null){
			if(event.phase==TickEvent.Phase.END){
				h.update();
			}else{
				if(h.isParagliding()){
					double v = ModCfg.paraglidingSpeed();
					event.player.jumpMovementFactor = (float)(DEFAULT_PARAGLIDING_SPEED*v);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		PlayerMovement h = event.getPlayer().getCapability(PlayerMovement.CAP).orElse(null);
		if(h instanceof ServerPlayerMovement){
			ServerPlayerMovement sh = (ServerPlayerMovement)h;
			sh.movementNeedsSync = true;
			sh.paraglidingNeedsSync = true;
			sh.vesselNeedsSync = true;
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event){
		PlayerEntity player = event.getPlayer();
		if(player instanceof ServerPlayerEntity){
			PlayerMovement h = event.getTarget().getCapability(PlayerMovement.CAP).orElse(null);
			if(h!=null){
				SyncParaglidingMsg msg = new SyncParaglidingMsg(h);
				if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Sending packet {} from player {} to player {}", msg, h.player, player);
				ModNet.NET.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), msg);
			}
		}
	}


	private static final class Client{
		public static PlayerMovement createPlayerMovement(PlayerEntity player){
			return player instanceof ClientPlayerEntity ? new ClientPlayerMovement(player) : new RemotePlayerMovement(player);
		}
	}
}
