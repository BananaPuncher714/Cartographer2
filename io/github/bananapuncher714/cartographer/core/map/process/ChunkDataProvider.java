package io.github.bananapuncher714.cartographer.core.map.process;

import org.bukkit.ChunkSnapshot;

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
}
