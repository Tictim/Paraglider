package tictim.paraglider.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.capabilities.wind.WindChunk;
import tictim.paraglider.capabilities.wind.WindNode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WindUpdater{
	private static final int XZ_RAD_HALF = 4;
	private static final int GROUND_Y_MIN = -2;
	private static final int GROUND_Y_MAX = 4;
	private static final int PARAGLIDING_Y_MIN = -11;
	private static final int PARAGLIDING_Y_MAX = 1;

	private final BlockPos.Mutable mpos = new BlockPos.Mutable();
	private final Set<WindChunk> modifiedChunks = new HashSet<>();

	public Set<WindChunk> getModifiedChunks(){
		return modifiedChunks;
	}

	/**
	 * Scans blocks around player and update wind chunks. Scan range is predefined.
	 */
	public void placeAround(PlayerEntity player){
		int x = MathHelper.floor(player.getPosX());
		int y = MathHelper.floor(player.getPosY());
		int z = MathHelper.floor(player.getPosZ());

		place(player.world,
				x-XZ_RAD_HALF,
				y+(player.isOnGround() ? GROUND_Y_MIN : PARAGLIDING_Y_MIN),
				z-XZ_RAD_HALF,
				x+XZ_RAD_HALF,
				y+(player.isOnGround() ? GROUND_Y_MAX : PARAGLIDING_Y_MAX),
				z+XZ_RAD_HALF);
	}

	/**
	 * Scans blocks in range and update wind chunks.
	 */
	public void place(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		Wind wind = Wind.of(world);
		if(wind==null){
			ParagliderMod.LOGGER.warn("Cannot place wind because there's no Wind capability associated with world {}", world);
			return;
		}
		WindWriter writer = new WindWriter(wind, world.getGameTime());

		for(int x = minX; x<=maxX; x++){
			for(int z = minZ; z<=maxZ; z++){
				writer.setXZ(x, z);

				boolean hasFireY = false;
				int fireY = 0;
				for(int y = minY; true; y++){
					mpos.setPos(x, y, z);
					BlockState state = world.getBlockState(mpos);
					boolean isWindSource = ModCfg.isWindSource(state);

					if(hasFireY){
						int height = y-fireY;
						if(height>=10||
								isWindSource||
								state.getMaterial().blocksMovement()||
								Block.hasEnoughSolidSide(world, mpos, Direction.DOWN)||
								Block.hasEnoughSolidSide(world, mpos, Direction.UP)){
							if(height>2) writer.wind(fireY, height);
							hasFireY = false;
						}else continue;
					}
					if(y>maxY) break;
					if(isWindSource){
						fireY = y;
						hasFireY = true;
					}
				}
				writer.end();
			}
		}

		modifiedChunks.addAll(writer.getModifiedChunks());
	}

	/**
	 * Checks if placed wind is still valid - that is still having wind source at root position, and didn't expired yet. All invalid winds will be removed.
	 */
	public void checkPlacedWind(World world){
		Wind wind = Wind.of(world);
		if(wind==null) return;

		for(WindChunk windChunk : wind.getWindChunks()){
			Collection<WindNode> allRootNodes = windChunk.getAllRootNodes();
			for(WindNode node : allRootNodes.toArray(new WindNode[0])){
				WindNode updated = validate(windChunk, node, world);
				if(updated!=node){
					if(updated==null) windChunk.removeAllNodesInXZ(node.x, node.z);
					else windChunk.putNode(updated);
				}
			}
		}
	}

	/**
	 * Actually checks things.
	 *
	 * @return {@code this} if still valid. Instance to replace it if invalid
	 */
	@Nullable private WindNode validate(WindChunk windChunk, WindNode node, World world){
		long gameTime = world.getGameTime();

		if(node.updatedTime!=gameTime){
			if(node.isExpired(gameTime)||
					!ModCfg.isWindSource(world.getBlockState(mpos.setPos(node.x, node.y, node.z)))){
				modifiedChunks.add(windChunk);
				return node.next!=null ? validate(windChunk, node.next, world) : null;
			}
			node.updatedTime = gameTime;
		}

		if(node.next!=null)
			node.next = validate(windChunk, node.next, world);
		return node;
	}
}
