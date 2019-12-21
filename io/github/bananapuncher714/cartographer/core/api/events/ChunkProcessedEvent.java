package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

/**
 * Called whenever a chunk has been finished processing
 * 
 * @author BananaPuncher714
 */
public class ChunkProcessedEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();

	protected final Minimap map;
	protected final ChunkLocation location;
	protected ChunkData data;
	
	public ChunkProcessedEvent( Minimap map, ChunkLocation location, ChunkData chunk ) {
		this.map = map;
		this.location = location;
		this.data = chunk;
	}
	
	public ChunkData getData() {
		return data;
	}

	public void setData( ChunkData data ) {
		this.data = data;
	}

	public Minimap getMinimap() {
		return map;
	}
	
	public ChunkLocation getLocation() {
		return location;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
