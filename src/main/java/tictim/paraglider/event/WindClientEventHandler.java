package tictim.paraglider.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.wind.Wind;
import tictim.paraglider.wind.WindChunk;
import tictim.paraglider.wind.WindNode;

import java.util.Random;

@EventBusSubscriber(modid = ParagliderMod.MODID, value = Dist.CLIENT)
public final class WindClientEventHandler{
	private WindClientEventHandler(){}

	private static final Random PARTICLE_RNG = new Random();

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.START||Minecraft.getInstance().isGamePaused()) return;

		ClientWorld world = Minecraft.getInstance().world;
		if(world==null) return;
		Wind wind = Wind.of(world);
		if(wind==null) return;

		for(WindChunk windChunk : wind.getWindChunks()){
			for(WindNode node : windChunk.getAllRootNodes()){
				do{
					if(PARTICLE_RNG.nextInt(6)==0)
						world.addOptionalParticle(ParticleTypes.FIREWORK,
								node.x+PARTICLE_RNG.nextDouble(),
								node.y+0.5,
								node.z+PARTICLE_RNG.nextDouble(),
								0,
								1,
								0);
				}while((node = node.next)!=null);
			}
		}
	}
}
