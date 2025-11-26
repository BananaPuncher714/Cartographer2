package io.github.bananapuncher714.cartographer.core.api.events.player;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class MapViewerCreateEvent extends MapViewerEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public MapViewerCreateEvent( MapViewer viewer ) {
		super( viewer );
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
