package tictim.paraglider.event;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindChunk;
import tictim.paraglider.wind.WindNode;

import static tictim.paraglider.api.ParagliderAPI.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public final class WindParticleEventHandler {
	private WindParticleEventHandler() {}

	// reusing one instance to store nodes for placing wind particles
	// because there's no way the method is called concurrently... right?
	private static final IntArrayList xyzList = new IntArrayList();

	private static double windParticleState;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event) {
		if (Minecraft.getInstance().isPaused()) return;
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		Wind wind = Wind.of(level);
		if (wind == null) return;

		windParticleState += ParagliderClientSettings.get().windParticleFrequency();
		double s2 = windParticleState - 1;
		if (s2 >= 0) windParticleState = s2;
		else return;

		for (WindChunk windChunk : wind.windChunks()) {
			for (var e : windChunk.nodes.byte2ObjectEntrySet()) {
				byte xz = e.getByteKey();
				WindNode node = e.getValue();

				do {
					xyzList.add(windChunk.x(xz));
					xyzList.add(node.y);
					xyzList.add(windChunk.z(xz));
					node = node.next;
				} while (node != null);
			}
		}

		int bound = 5 + xyzList.size() / 3;

		for (int i = 0; i < xyzList.size(); i += 3) {
			int x = xyzList.getInt(i);
			int y = xyzList.getInt(i + 1);
			int z = xyzList.getInt(i + 2);

			if (ParagliderUtils.PARTICLE_RNG.nextInt(bound) == 0) {
				level.addAlwaysVisibleParticle(ParticleTypes.FIREWORK, // TODO custom firework particle
						x + ParagliderUtils.PARTICLE_RNG.nextDouble(),
						y + 0.5, // TODO might need to move offset around if a full block source is used
						z + ParagliderUtils.PARTICLE_RNG.nextDouble(),
						0, 1, 0);
			}
		}

		xyzList.clear();
	}
}
