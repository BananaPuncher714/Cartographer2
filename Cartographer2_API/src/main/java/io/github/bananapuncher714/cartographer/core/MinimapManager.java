package io.github.bananapuncher714.cartographer.core;

import java.io.File;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.renderer.CartographerRenderer;

public class MinimapManager {
	protected Cartographer plugin;
	protected Map< String, Minimap > minimaps = null;

	protected CartographerLogger logger = null;
	
	public MinimapManager( Cartographer plugin ) {
	}
	
	public Map< String, Minimap > getMinimaps() {
		return null;
	}
	
	public boolean isMinimapItem( ItemStack item ) {
		return false;
	}
	
	public ItemStack getItemFor( Minimap map ) {
		return null;
	}
	
	public ItemStack getItemFor( Minimap map, int mapId ) {
		return null;
	}
	
	/**
	 * Get the {@link CartographerRenderer} of a MapView.
	 * 
	 * @param view
	 * The MapView to get it from.
	 * @return
	 * Null if the MapView provided is null, or does not contain a {@link CartographerRenderer}.
	 */
	public CartographerRenderer getRendererFrom( MapView view ) {
		return null;
	}
	
	public void update( ItemStack item ) {
	}
	
	public ItemStack update( ItemStack item, Minimap newMap ) {
		return null;
	}
	
	public void activate( Player player, MapInteraction interaction ) {
	}
	
	/**
	 * Change the given MapView to a Cartographer2 {@link Minimap}.
	 * 
	 * @param view
	 * @param map
	 */
	public void convert( MapView view, Minimap map ) {
	}
	
	protected void update() {
	}
	
	public void registerMinimap( Minimap minimap ) {
	}
	
	public Minimap constructNewMinimap( String id ) {
		return null;
	}
	
	public Minimap load( File dir ) {
		return null;
	}
	
	public Minimap constructNewMinimap( File dir ) {
		return null;
	}
	
	public void unload( Minimap map ) {
	}
	
	public void remove( Minimap map ) {
	}
	
	protected void terminate() {
	}
}
