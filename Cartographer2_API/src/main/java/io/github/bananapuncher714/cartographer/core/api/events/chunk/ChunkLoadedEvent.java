package io.github.bananapuncher714.cartographer.core.api.events.chunk;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

/**
 * Called after a chunk has been loaded from the file or externally.
 * 
 * @author BananaPuncher714
 */
public class ChunkLoadedEvent extends ChunkProcessedEvent {
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * Construct new ChunkLoadEvent for the provided {@link Minimap}, location, and {@link ChunkData}.
	 * 
	 * @param map
	 * The {@link Minimap} for which the {@link ChunkData} belongs. Cannot be null.
	 * @param location
	 * The {@link ChunkLocation} of the {@link ChunkData} provided. Cannot be null.
	 * @param chunk
	 * The loaded {@link ChunkData}. Should be complete. Cannot be null. 
	 */
	public ChunkLoadedEvent( Minimap map, ChunkLocation location, ChunkData chunk ) {
		super( map, location, chunk );
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
