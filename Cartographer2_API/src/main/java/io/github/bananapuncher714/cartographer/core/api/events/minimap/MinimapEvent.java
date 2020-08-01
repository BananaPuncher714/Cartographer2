package io.github.bananapuncher714.cartographer.core.api.events.minimap;

import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.api.events.CartographerEvent;
import io.github.bananapuncher714.cartographer.core.map.Minimap;

/**
 * Event pertaining to minimaps mainly.
 * 
 * @author BananaPuncher714
 */
public abstract class MinimapEvent extends CartographerEvent {
	private static final HandlerList handlers = new HandlerList();
	protected Minimap map;
	
	/**
	 * Construct a new MinimapEvent with the provided {@link Minimap}.
	 * 
	 * @param map
	 * Cannot be null.
	 */
	public MinimapEvent( Minimap map ) {
		Validate.notNull( map );
		this.map = map;
	}
	
	/**
	 * Get the {@link Minimap} involved in this event.
	 * 
	 * @return
	 * A non-null {@link Minimap}.
	 */
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
