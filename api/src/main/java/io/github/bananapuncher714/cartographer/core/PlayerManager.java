package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import io.github.bananapuncher714.cartographer.core.map.MapViewer;

public class PlayerManager {
	protected Cartographer plugin;
	protected File saveDirectory;
	
	protected Map< UUID, MapViewer > viewers;
	
	public PlayerManager( Cartographer plugin, File saveDirectory ) {
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
		return null;
	}
	
	public String getLocale( UUID uuid ) {
		return null;
	}
	
	public void setLocale( UUID uuid, String locale ) {
	}
	
	protected MapViewer load( UUID uuid ) {
		return null;
	}
	
	public void unload( UUID uuid ) {
	}
}
