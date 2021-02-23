package tictim.paraglider.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.capabilities.wind.WindChunk;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncWindMsg;
import tictim.paraglider.utils.WindUpdater;

import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

@EventBusSubscriber(modid = ParagliderMod.MODID)
public final class WindEventHandler{
	private WindEventHandler(){}

	private static final ResourceLocation WIND_KEY = new ResourceLocation(MODID, "wind");

	@SubscribeEvent
	public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event){
		event.addCapability(WIND_KEY, new Wind());
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event){
		if(event.phase!=TickEvent.Phase.END||!event.side.isServer()) return;

		WindUpdater placer = new WindUpdater();

		long gameTime = event.world.getGameTime();
		if(gameTime%4==0){
			List<? extends PlayerEntity> players = event.world.getPlayers();
			if(!players.isEmpty()){
				for(PlayerEntity player : players){
					if(Paraglider.isParaglider(player.getHeldItemMainhand()))
						placer.placeAround(player);
				}
			}
		}

		placer.checkPlacedWind(event.world);

		for(WindChunk windChunk : placer.getModifiedChunks()){
			Chunk chunk = event.world.getChunk(windChunk.getChunkPos().x, windChunk.getChunkPos().z);
			ModNet.NET.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new SyncWindMsg(windChunk));
		}
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event){
		IWorld iWorld = event.getWorld();
		if(!(iWorld instanceof World)) return;
		World world = (World)iWorld;
		Wind wind = Wind.of(world);
		if(wind!=null)
			wind.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event){
		ServerWorld world = event.getWorld();
		Wind wind = Wind.of(world);
		if(wind==null) return;
		ChunkPos pos = event.getPos();
		WindChunk windChunk = wind.get(pos);
		if(windChunk==null||windChunk.isEmpty()) return;
		Chunk chunk = world.getChunk(pos.x, pos.z);
		ModNet.NET.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new SyncWindMsg(windChunk));
	}
}
