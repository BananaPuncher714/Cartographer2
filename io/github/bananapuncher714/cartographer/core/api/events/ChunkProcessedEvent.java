package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.ChunkLocation;
import io.github.bananapuncher714.cartographer.core.map.ChunkData;

public class ChunkProcessedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	protected final ChunkLocation location;
	protected ChunkData data;
	
	public ChunkProcessedEvent( ChunkLocation location, ChunkData chunk ) {
		this.location = location;
		this.data = chunk;
	}
	
	public ChunkData getData() {
		return data;
	}

	public void setData( ChunkData data ) {
		this.data = data;
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
