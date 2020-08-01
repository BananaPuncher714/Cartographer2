package io.github.bananapuncher714.cartographer.core.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.cartographer.core.Cartographer;
import io.github.bananapuncher714.cartographer.core.api.ZoomScale;
import io.github.bananapuncher714.cartographer.core.map.Minimap;
import io.github.bananapuncher714.cartographer.core.map.menu.MapInteraction;
import io.github.bananapuncher714.cartographer.core.map.menu.MapMenu;

/**
 * Render a map and send the packet
 * 
 * @author BananaPuncher714
 */
public class CartographerRenderer extends MapRenderer {
	protected Thread renderer;

	protected Map< UUID, Double > scales = new HashMap< UUID, Double >();
	protected Map< UUID, PlayerSetting > settings = new HashMap< UUID, PlayerSetting >();
	
	protected Cartographer plugin;
	
	protected int id;
	
	// Keep this a string in case if we delete a minimap, so that this doesn't store the map in memory
	protected String mapId = null;
	
	protected long tick = 0;
	
	public CartographerRenderer( Cartographer plugin, Minimap map ) {
		super( true );
	}
	
	public boolean setPlayerMap( Player player, Minimap map ) {
		return false;
	}
	
	public double getScale( UUID uuid ) {
		return 0;
	}
	
	public void setScale( UUID uuid, ZoomScale scale ) {
	}
	
	public void setScale( UUID uuid, double blocksPerPixel ) {
	}
		
	public void setMapMenu( UUID uuid, MapMenu menu ) {
	}
	
	public MapMenu getMenu( UUID uuid ) {
		return null;
	}
	
	public Set< UUID > getActiveMapMenuViewers() {
		return null;
	}
	
	public void interact( Player player, MapInteraction interaction ) {
	}
	
	public boolean isViewing( UUID uuid ) {
		return false;
	}
	
	public void unregisterPlayer( Player player ) {
	}
	
	public Minimap getMinimap() {
		return null;
	}
	
	public void setMinimap( Minimap map ) {
	}
	
	public void resetCursorFor( Player player ) {
	}
	
	@Override
	public void render( MapView view, MapCanvas canvas, Player player ) {
	}

	public void terminate() {
	}
}
