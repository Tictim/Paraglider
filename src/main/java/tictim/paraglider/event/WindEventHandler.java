package tictim.paraglider.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.Paraglider;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncWindMsg;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindChunk;
import tictim.paraglider.wind.WindUpdater;

import java.util.List;

import static tictim.paraglider.ParagliderMod.MODID;

@EventBusSubscriber(modid = ParagliderMod.MODID)
public final class WindEventHandler{
	private WindEventHandler(){}

	private static final ResourceLocation WIND_KEY = new ResourceLocation(MODID, "wind");

	@SubscribeEvent
	public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<Level> event){
		event.addCapability(WIND_KEY, new Wind());
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent event){
		if(event.phase!=TickEvent.Phase.END||!event.side.isServer()) return;

		WindUpdater placer = new WindUpdater();

		long gameTime = event.level.getGameTime();
		if(gameTime%4==0){
			List<? extends Player> players = event.level.players();
			if(!players.isEmpty()){
				for(Player player : players){
					if(Paraglider.isParaglider(player.getMainHandItem()))
						placer.placeAround(player);
				}
			}
		}

		placer.checkPlacedWind(event.level);

		for(WindChunk windChunk : placer.getModifiedChunks()){
			LevelChunk chunk = event.level.getChunk(windChunk.getChunkPos().x, windChunk.getChunkPos().z);
			ModNet.NET.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new SyncWindMsg(windChunk));
		}
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event){
		if(!(event.getLevel() instanceof Level level)) return;
		Wind wind = Wind.of(level);
		if(wind!=null)
			wind.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event){
		ServerLevel level = event.getLevel();
		Wind wind = Wind.of(level);
		if(wind==null) return;
		ChunkPos pos = event.getPos();
		WindChunk windChunk = wind.get(pos);
		if(windChunk==null||windChunk.isEmpty()) return;
		LevelChunk chunk = level.getChunk(pos.x, pos.z);
		ModNet.NET.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new SyncWindMsg(windChunk));
	}
}
