package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

public class MinimapUnloadEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public MinimapUnloadEvent( Minimap map ) {
		super( map );
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
