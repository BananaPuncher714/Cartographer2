package io.github.bananapuncher714.cartographer.core.api.events.chunk;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.map.process.MapDataCache.ChunkProcessor;

/**
 * Called before a task gets submitted to the executor service to render a chunk
 * 
 * @author BananaPuncher714
 */
public class ChunkPreProcessEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();

	protected final ChunkLocation location;
	protected ChunkProcessor processor;
	
	public ChunkPreProcessEvent( ChunkLocation location, ChunkProcessor processor ) {
		this.location = location;
		this.processor = processor;
	}
	
	public ChunkProcessor getDataProcessor() {
		return processor;
	}

	public void setDataProcessor( ChunkProcessor processor ) {
		this.processor = processor;
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
