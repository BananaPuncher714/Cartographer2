package io.github.bananapuncher714.cartographer.core.api.events.chunk;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkData;

/**
 * Called whenever a chunk has been finished processing, not from file.
 * 
 * @author BananaPuncher714
 */
public class ChunkProcessedEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected final Minimap map;
	protected final ChunkLocation location;
	protected ChunkData data;
	
	/**
	 * Construct a new ChunkProcessedEvent for the given {@link Minimap} at the {@link ChunkLocation} provided.
	 * 
	 * @param map
	 * The {@link Minimap} for the data. Cannot be null.
	 * @param location
	 * The {@link ChunkLocation} for the data. Cannot be null.
	 * @param chunk
	 * The completed {@link ChunkData}. Cannot be null.
	 */
	public ChunkProcessedEvent( Minimap map, ChunkLocation location, ChunkData chunk ) {
		Validate.notNull( map );
		Validate.notNull( location );
		Validate.notNull( chunk );
		this.map = map;
		this.location = location;
		this.data = chunk;
	}
	
	/**
	 * Get the {@link ChunkData} for this event.
	 * 
	 * @return
	 * Non-null and completed {@link ChunkData}.
	 */
	public ChunkData getData() {
		return data;
	}

	/**
	 * Set the {@link ChunkData} for this event.
	 * 
	 * @param data
	 * Non-null and completed {@link ChunkData}.
	 */
	public void setData( ChunkData data ) {
		Validate.notNull( data );
		this.data = data;
	}

	/**
	 * Get the {@link Minimap} for this event.
	 * 
	 * @return
	 * Non-null {@link Minimap}.
	 */
	public Minimap getMinimap() {
		return map;
	}
	
	/**
	 * Get the {@link ChunkLocation} for this event.
	 * 
	 * @return
	 * Non-null {@link ChunkLocation}.
	 */
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
