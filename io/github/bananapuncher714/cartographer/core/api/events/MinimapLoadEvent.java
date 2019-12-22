package io.github.bananapuncher714.cartographer.core.api.events;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Called after a minimap gets loaded completely
 * 
 * @author BananaPuncher714
 */
public class MinimapLoadEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public MinimapLoadEvent( Minimap map ) {
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
