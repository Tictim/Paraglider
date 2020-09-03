package tictim.paraglider.utils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tictim.paraglider.ModCfg;
import tictim.paraglider.contents.WindEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.block.Block.hasSolidSide;

public final class WindUpdateHelper{
	private WindUpdateHelper(){}

	private static final int XZ_RAD_HALF = 4;
	private static final int GROUND_Y_MIN = -2;
	private static final int GROUND_Y_MAX = 4;
	private static final int PARAGLIDING_Y_MIN = -10;
	private static final int PARAGLIDING_Y_MAX = 1;

	private static final BlockPos.Mutable mpos = new BlockPos.Mutable();
	private static final Map<BlockPos, WindEntity> windMap = new HashMap<>();

	/*
	 * TODO NUKE THE FUCK OUT OF IT, I DON'T LIKE SINGLE PART OF IT, ALSO 99.99% WRONG AND BUGGED
	 */
	public static void generateWind(PlayerEntity player){
		World world = player.world;
		BlockPos origin = player.getPosition();
		final int yMax = player.onGround ? GROUND_Y_MAX : PARAGLIDING_Y_MAX;
		final int yMin = player.onGround ? GROUND_Y_MIN : PARAGLIDING_Y_MIN;
		windMap.clear();
		for(WindEntity e : world.getEntitiesWithinAABB(WindEntity.class, new AxisAlignedBB(
				origin.getX()-XZ_RAD_HALF, origin.getY()+yMin, origin.getZ()-XZ_RAD_HALF,
				origin.getX()+XZ_RAD_HALF+1, origin.getY()+yMax+1, origin.getZ()+XZ_RAD_HALF+1))){
			BlockPos p = e.getBlockPos();
			if(p!=null) windMap.put(p, e);
		}
		// if(!windMap.isEmpty()) System.out.println(windMap.keySet().stream().map(it -> "["+it.getX()+", "+it.getY()+", "+it.getZ()+"]").collect(Collectors.joining(", ")));

		int my = origin.getY()+yMax;

		for(int x = origin.getX()-XZ_RAD_HALF, _x = origin.getX()+XZ_RAD_HALF; x<=_x; x++){
			for(int z = origin.getZ()-XZ_RAD_HALF, _z = origin.getZ()+XZ_RAD_HALF; z<=_z; z++){
				int fireY = -1;
				for(int y = origin.getY()+yMin; true; y++){
					mpos.setPos(x, y, z);
					BlockState state = world.getBlockState(mpos);
					boolean isWindSource = ModCfg.isWindSource(state);

					if(fireY>=0){
						int height = y-fireY;
						if(height>=10||
								isWindSource||
								state.getMaterial().blocksMovement()||
								hasSolidSide(state, world, mpos, Direction.DOWN)||
								hasSolidSide(state, world, mpos, Direction.UP)){
							mpos.setY(fireY);
							WindEntity existingWind = windMap.remove(mpos);

							if(height>2){
								if(existingWind!=null){
									existingWind.setHeight(height);
									existingWind.extendLife();
								}else{
									WindEntity wind = new WindEntity(world);
									wind.setBlockPos(mpos);
									wind.setHeight(height);
									world.addEntity(wind);
								}
							}else if(existingWind!=null) existingWind.remove();
							fireY = -1;
							mpos.setY(y);
						}else{
							mpos.setY(fireY);
							WindEntity existingWind = windMap.remove(mpos);
							if(existingWind!=null){
								existingWind.remove();
							}
							mpos.setY(y);
							continue;
						}
					}
					if(y<=my){
						if(isWindSource) fireY = y;
						else{
							WindEntity existingWind = windMap.remove(mpos);
							if(existingWind!=null){
								existingWind.remove();
							}
						}
					}else break;
				}
			}
		}
	}
}
