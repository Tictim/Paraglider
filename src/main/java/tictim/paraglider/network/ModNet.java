package tictim.paraglider.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;

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
	}
}
