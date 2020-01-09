package io.github.bananapuncher714.cartographer.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;

import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class PlayerManager {
	protected Cartographer plugin;
	
	protected Map< UUID, MapViewer > viewers = new HashMap< UUID, MapViewer >();
	
	public PlayerManager( Cartographer plugin ) {
		this.plugin = plugin;
	}
	
	/**
	 * Get the {@link MapViewer} for the specified UUID.
	 * 
	 * @param uuid
	 * The UUID of a player, cannot be null.
	 * @return
	 * The MapViewer associated with the player.
	 */
	public MapViewer getViewerFor( UUID uuid ) {
		Validate.notNull( uuid );
		MapViewer viewer = viewers.get( uuid );
		if ( viewer == null ) {
			viewer = new MapViewer( uuid );
			viewers.put( uuid, viewer );
		}
		return viewer;
	}
}
