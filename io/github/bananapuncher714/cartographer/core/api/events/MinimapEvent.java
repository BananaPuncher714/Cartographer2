package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

public class MinimapEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected Minimap map;
	
	public MinimapEvent( Minimap map ) {
		this.map = map;
	}
	
	public Minimap getMinimap() {
		return map;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
