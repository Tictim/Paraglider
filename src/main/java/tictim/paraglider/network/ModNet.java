package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.client.DialogScreen;
import tictim.paraglider.dialog.Dialog;
import tictim.paraglider.dialog.DialogAction;
import tictim.paraglider.dialog.DialogActionArgs;
import tictim.paraglider.dialog.DialogActionException;
import tictim.paraglider.dialog.DialogContainer;

import java.util.Optional;
import java.util.function.Supplier;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModNet{
	private ModNet(){}

	public static final String NETVERSION = "1.0";
	public static final SimpleChannel NET = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "master"), () -> NETVERSION, NETVERSION::equals, NETVERSION::equals);

	public static void init(){
		NET.registerMessage(0, SyncMovementMsg.class,
				SyncMovementMsg::write, SyncMovementMsg::read,
				Client::handleSetMovement, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(1, SyncParaglidingMsg.class,
				SyncParaglidingMsg::write, SyncParaglidingMsg::read,
				Client::handleSetParagliding, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(2, SyncVesselMsg.class,
				SyncVesselMsg::write, SyncVesselMsg::read,
				Client::handleSetVessel, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(3, SyncWindMsg.class,
				SyncWindMsg::write, SyncWindMsg::read,
				Client::handleSyncWind, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(4, DialogActionRequestMsg.class,
				DialogActionRequestMsg::write, DialogActionRequestMsg::read,
				ModNet::handleDialogActionRequest, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NET.registerMessage(5, DialogActionResponseMsg.class,
				DialogActionResponseMsg::write, DialogActionResponseMsg::read,
				Client::handleDialogActionResponse, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(6, DialogActionErrorMsg.class,
				DialogActionErrorMsg::write, DialogActionErrorMsg::read,
				Client::handleDialogActionError, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static void handleDialogActionRequest(DialogActionRequestMsg msg, Supplier<NetworkEvent.Context> ctx){
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity sender = ctx.get().getSender();
		if(sender==null){
			ParagliderMod.LOGGER.error("Cannot handle packet {}, sender is null", msg);
			return;
		}
		ctx.get().enqueueWork(() -> {
			Container container = sender.openContainer;
			if(!(container instanceof DialogContainer)){
				ParagliderMod.LOGGER.error("Cannot handle packet {}, DialogContainer not open", msg);
				return;
			}
			DialogContainer dialogContainer = (DialogContainer)container;
			Dialog dialog = dialogContainer.getDialog();
			DialogAction dialogAction = dialog.getDialogAction(msg.id);
			if(dialogAction==null){
				ParagliderMod.LOGGER.error("Cannot handle packet {}, invalid dialog action ID {}", msg, msg.id);
				NET.send(PacketDistributor.PLAYER.with(() -> sender), new DialogActionErrorMsg(msg.id, "invalid dialog action ID "+msg.id));
				return;
			}
			DialogActionArgs args = new DialogActionArgs(dialogAction, sender, dialogContainer);
			try{
				dialogAction.getAction().perform(args);
			}catch(DialogActionException ex){
				args.respond(ex);
			}catch(RuntimeException ex){
				ParagliderMod.LOGGER.error("Unexpected error during execution of DialogAction {}", dialogAction.getId(), ex);
				args.respond(ex);
			}
		});
	}

	private static final class Client{
		private Client(){}

		public static void handleSetMovement(SyncMovementMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			PlayerMovement h = Minecraft.getInstance().player.getCapability(PlayerMovement.CAP).orElse(null);
			if(h!=null){
				if(ModCfg.traceMovementPacket()) ParagliderMod.LOGGER.debug("Received {}", msg);
				msg.copyTo(h);
			}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
		}

		public static void handleSetParagliding(SyncParaglidingMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(msg.playerId);
			if(player!=null){
				PlayerMovement h = player.getCapability(PlayerMovement.CAP).orElse(null);
				if(h!=null){
					if(h instanceof RemotePlayerMovement){
						if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Received {}", msg);
						((RemotePlayerMovement)h).setParagliding(msg.paragliding);
					}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability is found but not remote", msg);
				}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
			}else ParagliderMod.LOGGER.error("Couldn't find player with UUID {}", msg.playerId);
		}

		public static void handleSetVessel(SyncVesselMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			PlayerMovement h = Minecraft.getInstance().player.getCapability(PlayerMovement.CAP).orElse(null);
			if(h!=null){
				h.setHeartContainers(msg.heartContainers);
				h.setStaminaVessels(msg.staminaVessels);
				h.setStamina(msg.stamina);
			}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
		}

		public static void handleSyncWind(SyncWindMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ClientWorld world = Minecraft.getInstance().world;
			if(world==null) return;
			Wind wind = Wind.of(world);
			if(wind!=null) wind.put(msg.windChunk);
		}

		public static void handleDialogActionResponse(DialogActionResponseMsg msg, Supplier<NetworkEvent.Context> ctx){
			NetworkEvent.Context context = ctx.get();
			context.setPacketHandled(true);
			context.enqueueWork(() -> {
				Screen currentScreen = Minecraft.getInstance().currentScreen;
				if(currentScreen instanceof DialogScreen){
					((DialogScreen)currentScreen).processResponse(msg.id, msg.result);
				}else{
					ParagliderMod.LOGGER.warn("DialogAction response {} {} was ignored.", msg.id, msg.result);
				}
			});
		}

		public static void handleDialogActionError(DialogActionErrorMsg msg, Supplier<NetworkEvent.Context> ctx){
			NetworkEvent.Context context = ctx.get();
			context.setPacketHandled(true);
			context.enqueueWork(() -> {
				Screen currentScreen = Minecraft.getInstance().currentScreen;
				if(currentScreen instanceof DialogScreen){
					((DialogScreen)currentScreen).setError(msg.error);
				}else{
					ParagliderMod.LOGGER.warn("DialogAction error {} {} was ignored.", msg.id, msg.error);
				}
			});
		}
	}
}
