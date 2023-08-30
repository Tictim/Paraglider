package tictim.paraglider.wind;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.item.Paraglider;
import tictim.paraglider.client.ParagliderClientSettings;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.ArrayList;
import java.util.List;

public final class WindUtils{
	private WindUtils(){}

	public static void updateWind(@NotNull Level level){
		Wind wind = Wind.of(level);
		if(wind==null) return;

		long gameTime = level.getGameTime();
		if(gameTime%4==0){
			List<? extends Player> players = level.players();
			for(Player player : players){
				if(player.getMainHandItem().getItem() instanceof Paraglider){
					wind.placeAround(player);
				}
			}
		}

		wind.checkPlacedWind(level);

		if(level instanceof ServerLevel serverLevel){
			for(var it = wind.dirtyWindChunks().iterator(); it.hasNext(); ){
				long chunkPos = it.nextLong();
				WindChunk windChunk = wind.getChunk(chunkPos);
				if(windChunk==null) continue; // ???
				LevelChunk chunk = level.getChunk(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));
				ParagliderNetwork.get().syncWind(serverLevel.getServer(), chunk, windChunk);
			}
		}

		wind.dirtyWindChunks().clear();
	}

	// reusing one instance to store nodes for placing wind particles
	// because there's no way the method is called concurrently... right?
	@Environment(EnvType.CLIENT)
	private static List<WindNode> nodes;

	@Environment(EnvType.CLIENT)
	private static double windParticleState;

	@Environment(EnvType.CLIENT)
	public static void placeWindParticles(@NotNull Level level, @NotNull Wind wind){
		if(Minecraft.getInstance().isPaused()) return;
		windParticleState += ParagliderClientSettings.get().windParticleFrequency();
		double s2 = windParticleState-1;
		if(s2>=0) windParticleState = s2;
		else return;

		if(nodes==null) nodes = new ArrayList<>();

		for(WindChunk windChunk : wind.windChunks()){
			// intellij idea why are you like this
			for(@UnknownNullability WindNode node : windChunk.getAllRootNodes()){
				do{
					nodes.add(node);
				}while((node = node.next)!=null);
			}
		}

		int bound = 5+nodes.size();

		for(WindNode node : nodes){
			if(ParagliderUtils.PARTICLE_RNG.nextInt(bound)==0)
				level.addAlwaysVisibleParticle(ParticleTypes.FIREWORK,
						node.x+ParagliderUtils.PARTICLE_RNG.nextDouble(),
						node.y+0.5,
						node.z+ParagliderUtils.PARTICLE_RNG.nextDouble(),
						0, 1, 0);
		}

		nodes.clear();
	}
}
