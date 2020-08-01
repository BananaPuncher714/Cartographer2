package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

/**
 * Simulates vanilla map rendering, except not the water.
 * 
 * @author BananaPuncher714
 */
public class SimpleChunkProcessor implements ChunkDataProvider {
	protected MapDataCache cache;
	protected MinimapPalette palette;
	
	/**
	 * Construct a SimpleChunkProcessor with a cache and palette.
	 * 
	 * @param cache
	 * Cache containing other ChunkSnapshots that can be used. Cannot be null.
	 * @param palette
	 * A palette to get the colors for the blocks. Cannot be null.
	 */
	public SimpleChunkProcessor( MapDataCache cache, MinimapPalette palette ) {
	}
	
	@Override
	public ChunkData process( ChunkSnapshot snapshot ) {
		return null;
	}
	
	@Override
	public int process( Location location, MinimapPalette palette ) {
		return 0;
	}
}
