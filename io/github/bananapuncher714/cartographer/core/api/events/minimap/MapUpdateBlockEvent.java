package io.github.bananapuncher714.cartographer.core.api.events.minimap;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.map.MapPalette;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

/**
 * Called before updating of the block starts for a given {@link Minimap} with the {@link MapPalette} provided.
 * 
 * @author BananaPuncher714
 */
public class MapUpdateBlockEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	private Location location;
	private MinimapPalette palette;
	
	/**
	 * Construct a MapUpdateBlockEvent with the given {@link Minimap} and {@link MinimapPalette}.
	 * 
	 * @param map
	 * The {@link Minimap} updating the location. Cannot be null.
	 * @param location
	 * The location to update. Cannot be null.
	 * @param palette
	 * The {@link MinimapPalette} which will be used. Cannot be null.
	 */
	public MapUpdateBlockEvent( Minimap map, Location location, MinimapPalette palette ) {
		super( map );
		Validate.notNull( location );
		Validate.notNull( palette );
		this.location = location.clone();
		this.palette = palette;
	}
	
	/**
	 * Get the location involved in this event.
	 * 
	 * @return
	 * A new location.
	 */
	public Location getLocation() {
		return location.clone();
	}
	
	/**
	 * Get the {@link MinimapPalette} to be used.
	 * 
	 * @return
	 * A non-null {@link MinimapPalette}.
	 */
	public MinimapPalette getPalette() {
		return palette;
	}
	
	/**
	 * Set the {@link MinimapPalette} to be used.
	 * 
	 * @param palette
	 * Cannot be null.
	 */
	public void setPalette( MinimapPalette palette ) {
		Validate.notNull( palette );
		this.palette = palette;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
