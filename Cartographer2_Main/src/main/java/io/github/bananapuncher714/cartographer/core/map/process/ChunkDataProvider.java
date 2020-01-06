package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;

import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

/**
 * Process a ChunkSnapshot so it can be used by the map renderer.
 * 
 * @author BananaPuncher714
 */
public interface ChunkDataProvider {
	/**
	 * May be ran asynchronously.
	 * 
	 * @param snapshot
	 * A non-null snapshot to process.
	 * @return
	 * {@link ChunkData} that can be used.
	 */
	ChunkData process( ChunkSnapshot snapshot );
	
	/**
	 * Get the color at a particular location, should be called from the main thread.
	 * 
	 * @param location
	 * The location to render, cannot be null.
	 * @param palette
	 * The palette to use. Cannot be null.
	 * @return
	 * An integer color.
	 */
	int process( Location location, MinimapPalette palette );
}
