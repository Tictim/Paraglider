package tictim.paraglider.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindChunk;
import tictim.paraglider.wind.WindUtils;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class WindEventHandler{
	private WindEventHandler(){}

	@SubscribeEvent
	public static void onLevelLoad(LevelEvent.Load event){
		Wind.registerLevel(event.getLevel());
	}

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event){
		Wind.unregisterLevel(event.getLevel());
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent event){
		if(event.phase!=TickEvent.Phase.END||!event.side.isServer()) return;
		WindUtils.updateWind(event.level);
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event){
		if(!(event.getLevel() instanceof Level level)) return;
		Wind wind = Wind.of(level);
		if(wind!=null) wind.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event){
		ServerLevel level = event.getLevel();
		Wind wind = Wind.of(level);
		if(wind==null) return;
		ChunkPos pos = event.getPos();
		WindChunk windChunk = wind.getChunk(pos);
		if(windChunk==null||windChunk.isEmpty()) return;
		ParagliderNetwork.get().syncWind(level.getServer(), level.getChunk(pos.x, pos.z), windChunk);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.START) return;

		ClientLevel level = Minecraft.getInstance().level;
		if(level==null) return;
		Wind wind = Wind.of(level);
		if(wind==null) return;

		WindUtils.placeWindParticles(level, wind);
	}
}
