package io.github.bananapuncher714.cartographer.core.api.events.chunk;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

/**
 * Called whenever a chunk has been loaded from the file or externally
 * 
 * @author BananaPuncher714
 */
public class ChunkLoadedEvent extends ChunkProcessedEvent {
	private static final HandlerList handlers = new HandlerList();
	
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
