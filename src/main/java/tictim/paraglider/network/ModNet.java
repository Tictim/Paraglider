package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.client.screen.StatueBargainScreen;
import tictim.paraglider.contents.ModAdvancements;
import tictim.paraglider.recipe.bargain.BargainResult;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;
import tictim.paraglider.wind.Wind;

import java.util.Map;
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
		NET.registerMessage(4, BargainMsg.class,
				BargainMsg::write, BargainMsg::read,
				ModNet::handleBargain, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NET.registerMessage(5, UpdateBargainPreviewMsg.class,
				UpdateBargainPreviewMsg::write, UpdateBargainPreviewMsg::read,
				Client::handleUpdateBargainPreview, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(6, StatueDialogMsg.class,
				StatueDialogMsg::write, StatueDialogMsg::read,
				Client::handleStatueDialog, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		NET.registerMessage(7, SyncLookAtMsg.class,
				SyncLookAtMsg::write, SyncLookAtMsg::read,
				Client::handleSyncLookAt, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	private static void handleBargain(BargainMsg msg, Supplier<NetworkEvent.Context> ctx){
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if(player==null){
				ParagliderMod.LOGGER.error("Cannot handle BargainMsg: Wrong side");
				return;
			}
			if(!(player.containerMenu instanceof StatueBargainContainer c)) return; // Should be ignored
			for(StatueBargain bargain : c.getBargains()){
				if(!bargain.getId().equals(msg.bargain())) continue;
				BargainResult result = bargain.bargain(player, false);
				if(result.isSuccess()){
					ResourceLocation a = c.getAdvancement();
					if(a!=null){
						ModAdvancements.give(player, a, "bargain");
					}
				}
				c.sendDialog(bargain, result);
				return;
			}
			ParagliderMod.LOGGER.info("Ignoring invalid bargain {}", msg.bargain());
		});
	}

	private static final class Client{
		private Client(){}

		public static void handleSetMovement(SyncMovementMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			LocalPlayer player = Minecraft.getInstance().player;
			if(player==null) return;
			PlayerMovement h = PlayerMovement.of(player);
			if(h!=null){
				if(ModCfg.traceMovementPacket()) ParagliderMod.LOGGER.debug("Received {}", msg);
				msg.copyTo(h);
			}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
		}

		public static void handleSetParagliding(SyncParaglidingMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ClientLevel world = Minecraft.getInstance().level;
			if(world==null) return;
			Player player = world.getPlayerByUUID(msg.playerId());
			if(player!=null){
				PlayerMovement h = PlayerMovement.of(player);
				if(h!=null){
					if(h instanceof RemotePlayerMovement){
						if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Received {}", msg);
						((RemotePlayerMovement)h).setParagliding(msg.paragliding());
					}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability is found but not remote", msg);
				}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
			}else ParagliderMod.LOGGER.error("Couldn't find player with UUID {}", msg.playerId());
		}

		public static void handleSetVessel(SyncVesselMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			LocalPlayer player = Minecraft.getInstance().player;
			if(player==null) return;
			PlayerMovement h = PlayerMovement.of(player);
			if(h!=null){
				h.setHeartContainers(msg.heartContainers());
				h.setStaminaVessels(msg.staminaVessels());
				h.setStamina(msg.stamina());
			}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
		}

		public static void handleSyncWind(SyncWindMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ClientLevel world = Minecraft.getInstance().level;
			if(world==null) return;
			Wind wind = Wind.of(world);
			if(wind!=null) wind.put(msg.windChunk());
		}

		public static void handleUpdateBargainPreview(UpdateBargainPreviewMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				LocalPlayer player = Minecraft.getInstance().player;
				if(player==null) return;
				AbstractContainerMenu container = player.containerMenu;
				if(!(container instanceof StatueBargainContainer c)) return;
				for(Map.Entry<ResourceLocation, UpdateBargainPreviewMsg.Data> e : msg.getUpdated().entrySet()){
					c.setCanBargain(e.getKey(), e.getValue().canBargain());
					if(e.getValue().demands()!=null) c.setDemandPreview(e.getKey(), e.getValue().demands());
				}
			});
		}

		public static void handleStatueDialog(StatueDialogMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				Screen screen = Minecraft.getInstance().screen;
				if(!(screen instanceof StatueBargainScreen s)) return;
				s.setDialog(msg.text());
			});
		}

		public static void handleSyncLookAt(SyncLookAtMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				LocalPlayer player = Minecraft.getInstance().player;
				if(player==null) return;
				AbstractContainerMenu container = player.containerMenu;
				if(!(container instanceof StatueBargainContainer c)) return;
				c.setLookAt(msg.lookAt());
			});
		}
	}
}
