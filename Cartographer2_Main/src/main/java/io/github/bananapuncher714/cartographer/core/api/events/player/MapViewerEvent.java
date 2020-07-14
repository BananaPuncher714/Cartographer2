package io.github.bananapuncher714.cartographer.core.api.events.player;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class MapViewerEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	
	private MapViewer viewer;
	
	public MapViewerEvent( MapViewer viewer ) {
		this.viewer = viewer;
	}

	public MapViewer getViewer() {
		return viewer;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
