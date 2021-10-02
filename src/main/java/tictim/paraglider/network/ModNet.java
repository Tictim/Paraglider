package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.client.screen.StatueBargainScreen;
import tictim.paraglider.contents.ModAdvancements;
import tictim.paraglider.recipe.bargain.BargainResult;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;

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
			ServerPlayerEntity player = ctx.get().getSender();
			if(player==null){
				ParagliderMod.LOGGER.error("Cannot handle BargainMsg: Wrong side");
				return;
			}
			if(!(player.openContainer instanceof StatueBargainContainer)) return; // Should be ignored
			StatueBargainContainer c = (StatueBargainContainer)player.openContainer;
			for(StatueBargain bargain : c.getBargains()){
				if(!bargain.getId().equals(msg.bargain)) continue;
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
			ParagliderMod.LOGGER.info("Ignoring invalid bargain {}", msg.bargain);
		});
	}

	private static final class Client{
		private Client(){}

		public static void handleSetMovement(SyncMovementMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if(player==null) return;
			PlayerMovement h = PlayerMovement.of(player);
			if(h!=null){
				if(ModCfg.traceMovementPacket()) ParagliderMod.LOGGER.debug("Received {}", msg);
				msg.copyTo(h);
			}else ParagliderMod.LOGGER.error("Couldn't handle packet {}, capability not found", msg);
		}

		public static void handleSetParagliding(SyncParaglidingMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ClientWorld world = Minecraft.getInstance().world;
			if(world==null) return;
			PlayerEntity player = world.getPlayerByUuid(msg.playerId);
			if(player!=null){
				PlayerMovement h = PlayerMovement.of(player);
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
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if(player==null) return;
			PlayerMovement h = PlayerMovement.of(player);
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

		public static void handleUpdateBargainPreview(UpdateBargainPreviewMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				ClientPlayerEntity player = Minecraft.getInstance().player;
				if(player==null) return;
				Container container = player.openContainer;
				if(!(container instanceof StatueBargainContainer)) return;
				StatueBargainContainer c = (StatueBargainContainer)container;
				for(Map.Entry<ResourceLocation, UpdateBargainPreviewMsg.Data> e : msg.getUpdated().entrySet()){
					c.setCanBargain(e.getKey(), e.getValue().canBargain());
					if(e.getValue().getDemands()!=null) c.setDemandPreview(e.getKey(), e.getValue().getDemands());
				}
			});
		}

		public static void handleStatueDialog(StatueDialogMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				Screen screen = Minecraft.getInstance().currentScreen;
				if(!(screen instanceof StatueBargainScreen)) return;
				StatueBargainScreen s = (StatueBargainScreen)screen;
				s.setDialog(msg.text);
			});
		}

		public static void handleSyncLookAt(SyncLookAtMsg msg, Supplier<NetworkEvent.Context> ctx){
			ctx.get().setPacketHandled(true);
			ctx.get().enqueueWork(() -> {
				ClientPlayerEntity player = Minecraft.getInstance().player;
				if(player==null) return;
				Container container = player.openContainer;
				if(!(container instanceof StatueBargainContainer)) return;
				StatueBargainContainer c = (StatueBargainContainer)container;
				c.setLookAt(msg.lookAt);
			});
		}
	}
}
