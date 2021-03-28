package tictim.paraglider.utils;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.capabilities.wind.WindChunk;

public final class WindUtils{
	private WindUtils(){}

	public static boolean isInsideWind(World world, AxisAlignedBB boundingBox){
		return isInsideWind(world,
				MathHelper.floor(boundingBox.minX),
				MathHelper.floor(boundingBox.minY),
				MathHelper.floor(boundingBox.minZ),
				MathHelper.ceil(boundingBox.maxX),
				MathHelper.ceil(boundingBox.maxY),
				MathHelper.ceil(boundingBox.maxZ));
	}

	public static boolean isInsideWind(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
		Wind wind = Wind.of(world);
		if(wind==null) return false;

		int chunkXStart = minX >> 4;
		int chunkXEnd = maxX >> 4;
		int chunkZStart = minZ >> 4;
		int chunkZEnd = maxZ >> 4;

		for(int x = chunkXStart; x<=chunkXEnd; x++){
			for(int z = chunkZStart; z<=chunkZEnd; z++){
				WindChunk windChunk = wind.get(x, z);
				if(windChunk!=null&&windChunk.isInsideWind(minX, minY, minZ, maxX, maxY, maxZ)) return true;
			}
		}
		return false;
	}
}
