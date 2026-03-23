package tictim.paraglider.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import tictim.paraglider.network.ParagliderNetwork;
import tictim.paraglider.wind.WindLevel;
import tictim.paraglider.wind.WindChunk;
import tictim.paraglider.wind.WindLogic;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@EventBusSubscriber(modid = MODID)
public final class WindEventHandler {
	private WindEventHandler() {}

	@SubscribeEvent
	public static void onLevelLoad(LevelEvent.Load event) {
		WindLevel.registerLevel(event.getLevel());
	}

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		WindLevel.unregisterLevel(event.getLevel());
	}

	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Post event) {
		if (event.getLevel().isClientSide()) return;
		WindLogic.updateWind(event.getLevel());
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		if (!(event.getLevel() instanceof Level level)) return;
		WindLevel wind = WindLevel.of(level);
		if (wind != null) wind.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event) {
		ServerLevel level = event.getLevel();
		WindLevel wind = WindLevel.of(level);
		if (wind == null) return;
		ChunkPos pos = event.getPos();
		WindChunk windChunk = wind.getChunk(pos);
		if (windChunk == null || windChunk.isEmpty()) return;
		ParagliderNetwork.get().syncWind(level.getServer(), level.getChunk(pos.x(), pos.z()), windChunk);
	}
}
