package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Called before a minimap is about to be deleted
 * 
 * @author BananaPuncher714
 */
public class MinimapDeleteEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public MinimapDeleteEvent( Minimap map ) {
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
