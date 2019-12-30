package io.github.bananapuncher714.cartographer.core.api.events.minimap;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.palette.MinimapPalette;

public class MapUpdateBlockEvent extends MinimapEvent {
	private static final HandlerList handlers = new HandlerList();
	private Location location;
	private MinimapPalette palette;
	
	public MapUpdateBlockEvent( Minimap map, Location location, MinimapPalette palette ) {
		super( map );
		this.location = location.clone();
		this.palette = palette;
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public MinimapPalette getPalette() {
		return palette;
	}
	
	public void setPalette( MinimapPalette palette ) {
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
