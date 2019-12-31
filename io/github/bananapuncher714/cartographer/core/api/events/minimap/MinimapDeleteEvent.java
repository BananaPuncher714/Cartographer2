package io.github.bananapuncher714.cartographer.core.api.events.minimap;

import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Called before a {@link Minimap} is about to be deleted.
 * 
 * @author BananaPuncher714
 */
public class MinimapDeleteEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * The {@link Minimap} to be deleted.
	 * 
	 * @param map
	 * Cannot be null.
	 */
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
