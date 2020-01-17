package io.github.bananapuncher714.cartographer.core.api.events.chunk;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.map.process.ChunkProcessor;

/**
 * Called before a task gets submitted to the executor service to render a chunk.
 * 
 * @author BananaPuncher714
 */
public class ChunkPreProcessEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected final ChunkLocation location;
	protected ChunkProcessor processor;
	
	/**
	 * Construct a new ChunkPreProcessEvent at the given {@link ChunkLocation} with the {@link ChunkProcessor} provided.
	 * 
	 * @param location
	 * The location of the chunk. Cannot be null.
	 * @param processor
	 * The processor that will be used. Cannot be null.
	 */
	public ChunkPreProcessEvent( ChunkLocation location, ChunkProcessor processor ) {
		Validate.notNull( location );
		Validate.notNull( processor );
		this.location = location;
		this.processor = processor;
	}
	
	/**
	 * Get the {@link ChunkProcessor} that will be used to process the chunk data.
	 * 
	 * @return
	 * The {@link ChunkProcessor} should not be null.
	 */
	public ChunkProcessor getDataProcessor() {
		return processor;
	}

	/**
	 * Set the {@link ChunkProcessor} that will be used to process the chunk data.
	 * 
	 * @param processor
	 * Cannot be null.
	 */
	public void setDataProcessor( ChunkProcessor processor ) {
		Validate.notNull( processor );
		this.processor = processor;
	}

	/**
	 * Get the location of the chunk data to be processed.
	 * 
	 * @return
	 * The {@link ChunkLocation} of the chunk.
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
