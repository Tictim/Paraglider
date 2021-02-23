package tictim.paraglider.utils;

import tictim.paraglider.capabilities.wind.Wind;
import tictim.paraglider.capabilities.wind.WindChunk;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class WindWriter{
	private final Wind wind;
	private final long gameTime;

	private final Set<WindChunk> modifiedChunks = new HashSet<>();

	private int x = Integer.MAX_VALUE;
	private int z = Integer.MAX_VALUE;

	@Nullable private WindChunk windChunk;

	private boolean posSet;

	public WindWriter(Wind wind, long gameTime){
		this.wind = wind;
		this.gameTime = gameTime;
	}

	public Set<WindChunk> getModifiedChunks(){
		return modifiedChunks;
	}

	/**
	 * Sets XZ position that writer is working on.
	 */
	public void setXZ(int x, int z){
		if(posSet) end();
		posSet = true;
		if((this.x >> 4)!=(x >> 4)||(this.z >> 4)!=(z >> 4)){
			windChunk = null;
		}

		this.x = x;
		this.z = z;
	}

	/**
	 * Sets wind at specified Y position, with given height. XZ position must be set first.
	 */
	public void wind(int y, int height){
		expectPosSet();
		if(windChunk==null) windChunk = wind.getOrCreate(x >> 4, z >> 4);
		if(windChunk.add(x, y, z, height, gameTime))
			markModified();
	}

	/**
	 * Signifies end of writing wind at current XZ position. Any wind data at XZ position that isn't written from this Writer will be erased.
	 */
	public void end(){
		expectPosSet();
		posSet = false;
	}

	private void expectPosSet(){
		if(!posSet) throw new IllegalStateException("XZ position not set");
	}

	private void markModified(){
		modifiedChunks.add(windChunk);
	}
}
